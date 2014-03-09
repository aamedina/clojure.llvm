(ns clojure.llvm.api
  (:require [clojure.reflect :refer [reflect]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:import [llvm
            Llvm34Library
            LLVMOpInfo1
            LLVMOpInfoSymbol1
            LLVMMCJITCompilerOptions]
           [com.sun.jna Function]))

(set! *print-meta* true)

(defn strip-deprecated-methods
  [path]
  (->> (reduce (fn [new-file-lines line]
                 (if (seq new-file-lines)
                   (if-not (re-find #"@Deprecated" (peek new-file-lines))
                     (conj new-file-lines line)
                     (pop new-file-lines))
                   (conj new-file-lines line)))
               [] (line-seq (io/reader (io/file path))))
       (str/join "\n")
       (spit (io/file path))))

(defmacro gen-inline-llvm-c-types
  []
  `(do ~@(for [class (.getDeclaredClasses Llvm34Library)]
           `(def ~(symbol (last (str/split (.getName class) #"\$"))) ~class))))

(defn inline-type
  [sym]
  (condp = sym
    'char Character/TYPE
    'short Short/TYPE
    'int Integer/TYPE
    'long Long/TYPE
    'float Float/TYPE
    'double Double/TYPE
    'byte Byte/TYPE
    'boolean Boolean/TYPE
    'void Void/TYPE
    (if (and sym (re-find #"<>" (name sym)))
      `(-> ~(inline-type (symbol (first (str/split "long<>" #"<>"))))
           into-array
           class)
      sym)))

(defn type-pred
  [sym type]
  (condp = (str type)
    "short" `(number? ~sym)
    "int" `(integer? ~sym)
    "float" `(float? ~sym)
    "double" `(number? ~sym)))

(defn get-function
  [fn-name]
  (Function/getFunction "llvm-3.4" (name fn-name)))

(defmacro gen-inline-llvm-c-bindings
  "A macro which generates and defs in the calling namespace inline Clojure
   functions, maps, and classes which directly correspond to the bindings
   found in the LLVM-C native library."
  []
  (let [{:keys [bases flags members] :as llvm} (reflect Llvm34Library)]
    `(do ~@(for [member members]
             (let [ret (inline-type (:return-type member))
                   hinted-name (vary-meta (:name member) assoc :tag ret)
                   arg-hints (map inline-type (:parameter-types member))
                   hinted-args (map (fn [arg-hint]
                                      (with-meta (gensym)
                                        {:tag arg-hint}))
                                    arg-hints)
                   pre-conditions {:pre (mapv (fn [arg-sym type-hint]
                                                (let [arg (gensym "arg")]
                                                  `(= (type ~arg) ~type-hint)))
                                              hinted-args arg-hints)}
                   post-conditions {:post [(if (= ret Void/TYPE)
                                             `(nil? ~(symbol "%"))
                                             `(= (type ~(symbol "%")) ~ret))]}]
               (cond
                 (and (instance? clojure.reflect.Method member)
                      (contains? (:flags member) :static))
                 `(defn ^:static ~hinted-name
                    [~@hinted-args]
                    (. Llvm34Library ~(:name member)))
                 (instance? clojure.reflect.Method member)
                 `(defn ~hinted-name
                    [~@hinted-args]
                    (. Llvm34Library/INSTANCE ~(:name member)))
                 (and (instance? clojure.reflect.Field member)
                      (contains? (:flags member) :static))
                 `(def ^:static ~hinted-name
                    (. Llvm34Library
                       ~(symbol (str "-" (name (:name member))))))
                 (instance? clojure.reflect.Field member)
                 `(def ~hinted-name
                    (. Llvm34Library
                       ~(symbol (str "-" (name (:name member))))))))))))

(defn split-by
  "Split a collection according to some predicate."
  [pred coll]
  ((juxt #(filter pred %) #(remove pred %)) coll))

(gen-inline-llvm-c-types)
(gen-inline-llvm-c-bindings)
