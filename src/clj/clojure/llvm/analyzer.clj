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
            [clojure.tools.analyzer.jvm :as jvm]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:import java.lang.StringBuilder
           java.io.File))

(set! *warn-on-reflection* true)

(defn empty-env []
  (jvm/empty-env))

(defn analyze
  [form env]
  (jvm/analyze form env))
