(ns clojure.llvm.compiler
  (:refer-clojure :exclude [eval macroexpand-1 macroexpand load])
  (:use clojure.llvm.api)
  (:require [clojure.tools.analyzer :refer [macroexpand-1 macroexpand]]
            [clojure.llvm.analyzer :as ana]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]
            [clojure.tools.namespace.repl :refer [refresh]]))
