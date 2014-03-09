(ns clojure.llvm
  (:gen-class))

(System/setProperty "jna.library.path"
                    "/usr/local/opt/llvm/lib:/usr/local/lib:/usr/lib")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
