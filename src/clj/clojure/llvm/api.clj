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
            LLVMMCJITCompilerOptions]))

(set! *print-meta* true)

(defn resolve-class
  [type]
  (case type
    Integer/TYPE Integer
    Long/TYPE Long
    Character/TYPE Character
    Boolean/TYPE Boolean
    Float/TYPE Float
    Double/TYPE Double
    Void/TYPE Void
    type))

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

(defmacro gen-inline-llvm-c-bindings
  "A macro which generates and defs in the calling namespace inline Clojure
   functions, maps, and classes which directly correspond to the bindings
   found in the LLVM-C native library."
  []
  (let [{:keys [bases flags members] :as llvm} (reflect Llvm34Library)]
    `(do ~@(for [member members]
             (let [args (:parameter-types member)]
               `(defn ~(vary-meta (:name member) assoc :tag
                                  (resolve-class (:return-type member)))
                  [~@(map (fn [arg-type]
                            (vary-meta (gensym (resolve-class arg-type))
                                       assoc :tag (resolve-class arg-type)))
                          args)]
                  ~member))))))

(defn split-by
  "Split a collection according to some predicate."
  [pred coll]
  ((juxt #(filter pred %) #(remove pred %)) coll))

(gen-inline-llvm-c-types)
;; (gen-inline-llvm-c-bindings)
