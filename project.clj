(defproject google_authenticator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [commons-codec/commons-codec "1.12"]
                 [cli-matic "0.1.14"]]
  :main google-authenticator.core
  :java-source-paths ["java/src"]
  :repl-options {:init-ns google-authenticator.core})
