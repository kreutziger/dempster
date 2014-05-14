(defproject dempster "0.1.0-SNAPSHOT"
  :description "Dempster Shafers Regel Implementation"
  :url "http://wwwlehre.dhbw-stuttgart.de/~reichard/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot dempster.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
