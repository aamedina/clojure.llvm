(ns clojure.llvm.api
  (:require [clojure.reflect :refer [reflect]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:import [com.sun.jna Pointer Function Memory Native]
           [com.sun.jna.ptr PointerByReference]))

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

(defn get-function
  [fn-name]
  (Function/getFunction "llvm-3.4" (name fn-name)))

(defmulti gen-inline-def
  (fn [{:keys [flags] :as member}]
    [(class member) (set/intersection #{:static} flags)]))

(defmethod gen-inline-def [clojure.reflect.Method #{}]
  [{:keys [name parameter-types] :as member}]
  `(defn ~name ~parameter-types
     (. llvm.Llvm34Library/INSTANCE ~name ~@parameter-types)))

(defmethod gen-inline-def [clojure.reflect.Method #{:static}]
  [{:keys [name parameter-types] :as member}]
  `(defn ~name ~parameter-types
     (. llvm.Llvm34Library ~name ~@parameter-types)))

(defmethod gen-inline-def [clojure.reflect.Field #{}]
  [{:keys [name type] :as member}]
  `(def ~name (. llvm.Llvm34Library/INSTANCE ~name)))

(defmethod gen-inline-def [clojure.reflect.Field #{:static}]
  [{:keys [name type] :as member}]
  `(def ~name (. llvm.Llvm34Library ~name)))

(defn prep-method-or-field
  [member]
  (if (instance? clojure.reflect.Method member)
    (-> (update-in member [:return-type] inline-type)
        ((fn [{:keys [return-type] :as member}]
           (update-in member [:name] vary-meta assoc :tag return-type)))
        (update-in [:parameter-types] #(mapv inline-type %))
        (update-in [:parameter-types] #(mapv (fn [hint]
                                               (with-meta (gensym)
                                                 {:tag hint})) %)))
    (-> (update-in member [:type] inline-type)
        ((fn [{:keys [type] :as member}]
           (update-in member [:name] vary-meta assoc :tag type))))))

(defmacro gen-inline-llvm-c-bindings
  "A macro which generates and defs in the calling namespace inline Clojure
   functions and classes which directly correspond to the bindings
   found in the LLVM-C native library."
  []
  (let [{:keys [bases flags members] :as llvm} (reflect llvm.Llvm34Library)]
    `(do ~@(for [member (map prep-method-or-field members)]
             (gen-inline-def member)))))

(defn split-by
  "Split a collection according to some predicate."
  [pred coll]
  ((juxt #(filter pred %) #(remove pred %)) coll))

;; (gen-inline-llvm-c-bindings)

(defprotocol IPointer
  (-pointer [val]))

(defn pointer
  "Returns a new pointer of platform defined width."
  ([] (Pointer. (Native/malloc Pointer/SIZE)))
  ([val] (-pointer val)))
