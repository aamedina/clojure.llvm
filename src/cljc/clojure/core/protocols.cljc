(ns clojure.core.protocols)

;; (defprotocol CollReduce
;;   (coll-reduce [coll f] [coll fval]))

;; (defprotocol InternalReduce
;;   (internal-reduce [seq f start]))

;; (defprotocol IKVReduce
;;   (kv-reduce [amap f init]))

(defprotocol ICounted
  (-count [_]))
