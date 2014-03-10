(ns clojure.llvm.analyzer
  (:refer-clojure :exclude [var? macroexpand-1])
  (:require [clojure.tools.analyzer :as ana]
            [clojure.tools.analyzer
             [utils :refer [ctx resolve-var -source-info resolve-ns]]
             [ast :refer [walk prewalk postwalk cycling]]]
            [clojure.tools.analyzer.passes
             [source-info :refer [source-info]]
             [cleanup :refer [cleanup]]
             [elide-meta :refer [elide-meta]]
             [warn-earmuff :refer [warn-earmuff]]
             [collect :refer [collect collect-closed-overs]]
             [add-binding-atom :refer [add-binding-atom]]
             [uniquify :refer [uniquify-locals]]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:import java.lang.StringBuilder
           java.io.File))

(set! *warn-on-reflection* true)

(def ^:dynamic *unchecked-if* (atom false))
(def ^:dynamic *warnings*
  {:unprovided true
   :undeclared-var true
   :undeclared-ns true
   :undeclared-ns-form true
   :redef true
   :dynamic true
   :fn-var true
   :fn-arity true
   :fn-deprecated true
   :protocol-deprecated true
   :undeclared-protocol-symbol true
   :invalid-protocol-symbol true
   :multiple-variadic-overloads true
   :variadic-max-arity true
   :overload-arity true
   :extending-base-js-type true
   :invoke-ctor true
   :invalid-arithmetic true})

(defmulti error-message (fn [warning-type & _] warning-type))

(defmethod error-message :unprovided
  [warning-type info]
  (str "Required namespace not provided for "
       (clojure.string/join " " (:unprovided info))))

(defmethod error-message :undeclared-var
  [warning-type info]
  (str "Use of undeclared Var " (:prefix info) "/" (:suffix info)))

(defmethod error-message :undeclared-ns
  [warning-type info]
  (str "No such namespace: " (:ns-sym info)))

(defmethod error-message :dynamic
  [warning-type info]
  (str (:name info) " not declared ^:dynamic"))

(defmethod error-message :redef
  [warning-type info]
  (str (:sym info) " already refers to: "
       (symbol (str (:ns info)) (str (:sym info)))
       " being replaced by: "
       (symbol (str (:ns-name info)) (str (:sym info)))))

(defmethod error-message :fn-var
  [warning-type info]
  (str (symbol (str (:ns-name info)) (str (:sym info)))
    " no longer fn, references are stale"))

(defmethod error-message :fn-arity
  [warning-type info]
  (str "Wrong number of args (" (:argc info) ") passed to "
    (or (:ctor info)
      (:name info))))

(defmethod error-message :fn-deprecated
  [warning-type info]
  (str (-> info :fexpr :info :name) " is deprecated."))

(defmethod error-message :undeclared-ns-form
  [warning-type info]
  (str "Referred " (:type info) " " (:lib info) "/" (:sym info)
       " does not exist"))

(defmethod error-message :protocol-deprecated
  [warning-type info]
  (str "Protocol " (:protocol info) " is deprecated"))

(defmethod error-message :undeclared-protocol-symbol
  [warning-type info]
  (str "Can't resolve protocol symbol " (:protocol info)))

(defmethod error-message :invalid-protocol-symbol
  [warning-type info]
  (str "Symbol " (:protocol info) " is not a protocol"))

(defmethod error-message :multiple-variadic-overloads
  [warning-type info]
  (str (:name info) ": Can't have more than 1 variadic overload"))

(defmethod error-message :variadic-max-arity
  [warning-type info]
  (str (:name info) ": Can't have fixed arity function with more params than"
       " variadic function"))

(defmethod error-message :overload-arity
  [warning-type info]
  (str (:name info) ": Can't have 2 overloads with same arity"))

(defmethod error-message :extending-base-js-type
  [warning-type info]
  (str "Extending an existing JavaScript type - use a different symbol name "
       "instead of " (:current-symbol info) " e.g " (:suggested-symbol info)))

(defmethod error-message :invalid-arithmetic
  [warning-type info]
  (str (:js-op info) ", all arguments must be numbers, got " (:types info)
       " instead."))

(defmethod error-message :invoke-ctor
  [warning-type info]
  (str "Cannot invoke type constructor " (-> info :fexpr :info :name)
       " as function "))

(defn message [env s]
  (str s (when (:line env)
           (str " at line " (:line env) " " *file*))))

(defn default-warning-handler [warning-type env extra]
  (when (warning-type *warnings*)
    (when-let [s (error-message warning-type extra)]
      (binding [*out* *err*]
        (println (message env (str "WARNING: " s)))))))

(def ^:dynamic *warning-handlers*
  [default-warning-handler])

(defmacro with-warning-handlers [handlers & body]
  `(binding [*warning-handlers* ~handlers] ~@body))

(defn munge-path [ss]
  (clojure.lang.Compiler/munge (str ss)))

(defn ns->relpath [s]
  (str (str/replace (munge-path s) \. \/) ".cljc"))

(def constant-counter (atom 0))

(defn gen-constant-id [value]
  (let [prefix (cond
                 (keyword? value) "constant$keyword$"
                 :else
                 (throw (Exception. (str "constant type " (type value)
                                         " not supported"))))]
    (symbol (str prefix (swap! constant-counter inc)))))

(defmacro no-warn [& body]
  (let [no-warnings (zipmap (keys *warnings*) (repeat false))]
    `(binding [*warnings* ~no-warnings]
       ~@body)))

(defmacro all-warn [& body]
  (let [all-warnings (zipmap (keys *warnings*) (repeat true))]
    `(binding [*warnings* ~all-warnings]
       ~@body)))

(defn get-line [x env]
  (or (-> x meta :line) (:line env)))

(defn get-col [x env]
  (or (-> x meta :column) (:column env)))

(def vars (atom {}))

(def specials
  (into ana/specials '#{}))

(def empty-ns
  {:mappings {}
   :aliases {}
   :ns nil})

(def default-namespaces
  {'clojure.core (assoc empty-ns :ns 'clojure.core)
   'user (assoc empty-ns :ns 'user)})

(defn empty-env []
  {:ns 'user
   :context :expr
   :locals {}
   :namespaces (atom default-namespaces)})

(defmacro debug-prn
  [& args]
  `(.println *err* (str ~@args)))

(defn desugar-host-expr
  [[op & expr :as form]]
  (if (symbol? op)
    (let [opname (name op)]
      (cond
        (= (first opname) \.)
        (let [[target & args] expr
              args (list* (symbol (subs opname 1)) args)]
          (with-meta (list '. target (if (= 1 (count args)) (first args) args))
            (meta form)))
        (= (last opname) \.)
        (let [sym (symbol (subs opname 0 (dec (count opname))))
              desugared-form (list* 'new sym expr)]
          (with-meta desugared-form
            (meta form)))
        :else form))
    form))

(defn macroexpand-1
  [form env]
  (if (seq? form)
    (let [[op & args] form]
      (if (specials op)
        form
        (let [v (resolve-var op env)
              m (meta v)
              local? (-> env :locals (get op))
              macro? (and (not local?) (:macro m))
              inline-arities-fn (:inline-arities m)
              inline? (and (not local?)
                           (or (not inline-arities-fn)
                               (inline-arities-fn (count args)))
                           (:inline m))
              t (:tag m)]
          (if macro?
            (apply v form env (rest form))
            (desugar-host-expr form)))))
    (desugar-host-expr form)))

(defn create-var
  [sym {:keys [ns]}]
  (get (swap! vars assoc sym nil) sym))

(defn var?
  [var]
  (contains? @vars var))

(defn run-passes
  [ast]
  (-> ast
      uniquify-locals
      add-binding-atom
      (prewalk (fn [ast]
                 (-> ast warn-earmuff source-info elide-meta elide-meta)))
      ((collect {:what #{:constants :callsites}
                 :where #{:deftype :reify :fn}
                 :top-level? false}))
      (collect-closed-overs {:what #{:closed-overs}
                             :where #{:deftype :reify :fn :loop}
                             :top-level? false})
      (prewalk cleanup)))

(defn analyze
  [form env]
  (binding [ana/macroexpand-1 macroexpand-1
            ana/create-var create-var
            ana/parse ana/-parse
            ana/var? var?]
    (run-passes (ana/analyze form env))))
