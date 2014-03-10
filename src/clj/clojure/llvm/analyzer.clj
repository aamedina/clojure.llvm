(ns clojure.llvm.analyzer
  (:require [clojure.tools.analyzer :as ana]
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
            [clojure.llvm.env :as env]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as reader-types])
  (:import java.lang.StringBuilder
           java.io.File))

(set! *warn-on-reflection* true)

(def vars (atom {}))

(def specials
  (into ana/specials '#{}))

(defn create-var
  [sym {:keys [ns]}]
  (get (swap! vars assoc sym nil) sym))

(defn analyze
  [form env]
  (binding [ana/macroexpand-1 macroexpand-1
            ana/create-var create-var
            ana/parse ana/-parse
            ana/var? (fn [var] (contains? @vars var))]
    (ana/analyze form env)))
