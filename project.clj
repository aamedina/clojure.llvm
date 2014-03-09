(defproject clojure.llvm "0.1.0-SNAPSHOT"
  :description "A LLVM bytecode compiler for Clojure"
  :url "http://github.com/aamedina/clojure.llvm"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories 
  {"sonatype" "http://oss.sonatype.org/content/repositories/snapshots"}
  :dependencies [[org.clojure/clojure "1.6.0-beta2"]
                 [org.clojure/tools.analyzer "0.1.0-beta7"]
                 [org.clojure/tools.analyzer.jvm "0.1.0-beta7"]
                 [org.clojure/tools.emitter.jvm "0.1.0-alpha2"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [net.java.dev.jna/jna "4.1.0"]
                 [com.nativelibs4java/jnaerator "0.12-SNAPSHOT"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :main ^:skip-aot clojure.llvm
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
