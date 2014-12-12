(defproject fp2 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.19"]]
  :main ^:skip-aot fp2.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
