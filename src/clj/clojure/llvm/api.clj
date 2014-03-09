(ns clojure.llvm.api
  (:require [clojure.reflect :refer [reflect]]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import [llvm
            Llvm34Library
            LLVMOpInfo1
            LLVMOpInfoSymbol1
            LLVMMCJITCompilerOptions]))

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
               `(defn ~(:name member) [~@(repeatedly (count args) gensym)]
                  ~member))))))

(defn split-by
  "Split a collection according to some predicate."
  [pred coll]
  ((juxt #(filter pred %) #(remove pred %)) coll))

(gen-inline-llvm-c-types)
;; (gen-inline-llvm-c-bindings)
