(ns clojure.llvm.compiler
  (:refer-clojure :exclude [eval macroexpand-1 macroexpand load])
  (:require [clojure.tools.analyzer :refer [macroexpand-1 macroexpand]]
            [clojure.llvm.analyzer :as ana]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]))

(defn eval
  ([form] (eval form false))
  ([form debug?]
     (binding [macroexpand-1 ana/macroexpand-1]
       (macroexpand form (ana/empty-env)))))

(defn load
  ([resource] (load resource false))
  ([resource debug?]
     (clojure.core/load resource)))
