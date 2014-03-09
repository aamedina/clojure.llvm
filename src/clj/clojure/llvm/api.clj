(ns clojure.llvm.api)

(defmacro gen-inline-llvm-c-bindings
  "A macro which generates and defs in the calling namespace inline Clojure
   functions, maps, and classes which directly correspond to the bindings 
   found in the LLVM-C native library."
  [])

(gen-inline-llvm-c-bindings)
