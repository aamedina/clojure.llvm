(ns clojure.llvm.api
  (:require [clojure.reflect :refer [reflect]]
            [clojure.set :as set])
  (:import [llvm
            Llvm34Library
            LLVMOpInfo1
            LLVMOpInfoSymbol1
            LLVMMCJITCompilerOptions]))

(defmacro gen-inline-llvm-c-bindings
  "A macro which generates and defs in the calling namespace inline Clojure
   functions, maps, and classes which directly correspond to the bindings 
   found in the LLVM-C native library."
  []
  (let [{:keys [bases flags members]} (reflect Llvm34Library)]
    flags))

(gen-inline-llvm-c-bindings)
