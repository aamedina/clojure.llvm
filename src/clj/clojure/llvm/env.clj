(ns clojure.llvm.env
  (:refer-clojure :exclude [ensure]))

(def ^:dynamic *compiler* nil)

(defn default-compiler-env
  ([] (default-compiler-env {}))
  ([options] (atom {:options options})))

(defmacro with-compiler-env
  [env & body]
  `(let [env# ~env
         env# (cond
                (map? env#) (atom env#)
                (and (instance? clojure.lang.Atom env#) (map? @env#)) env#
                :else (throw (IllegalArgumentException.
                              (str "Compiler environment must be a map or atom"
                                   " containing a map, not " (class env#)))))]
     (binding [*compiler* env#] ~@body)))

(defmacro ensure
  [& body]
  `(let [val# *compiler*]
     (when (nil? val#)
       (push-thread-bindings
        (hash-map (var *compiler*) (default-compiler-env))))
     (try ~@body
          (finally (when (nil? val#)
                     (pop-thread-bindings))))))
