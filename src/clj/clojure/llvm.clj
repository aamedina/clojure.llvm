(ns clojure.llvm
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.llvm.api :as api]))

(System/setProperty "jna.library.path"
                    "/usr/local/opt/llvm/lib:/usr/local/lib:/usr/lib")

(def jnaerator-cmd
  ["java" "-jar" "resources/jnaerator.jar" "resources/config.jnaerator"])

(defn -main
  [& args]
  (println (:out (apply sh jnaerator-cmd))))
