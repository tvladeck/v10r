(defproject v10r "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-alpha4"]
                 [clatrix/clatrix "0.1.0-SNAPSHOT"]
                 [incanter/incanter "1.3.0-SNAPSHOT"
                      :exclusions [incanter/incanter-core]]
                 [uk.co.forward/incanter-core-jblas "1.3.0-SNAPSHOT"]
                 [com.taoensso/carmine "0.10.1"]
                 [jblas/jblas "1.2.1"]
                 [jblas/native "1.2.0"]
                 [criterium "0.3.0"]
                 [org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]
  :java-source-paths ["src/v10r/java"]
  :main v10r.compute)
