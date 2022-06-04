(defproject bank-service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [duct/core "0.8.0"]
                 [duct/handler.sql "0.4.0"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/module.web "0.7.3"]
                 [org.postgresql/postgresql "42.2.19"]
                 [hden/handler.honeysql-postgres "0.2.0"]
                 [buddy/buddy-hashers "1.8.158" :exclusions [[cheshire]]]
                 [duct/middleware.buddy "0.2.0" :exclusions [cheshire com.google.code.findbugs/jsr305]]
                 [duct/logger.timbre "0.5.0"]]
  :plugins [[duct/lein-duct "0.12.3"]]
  :main ^:skip-aot bank-service.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :middleware     [lein-duct.plugin/middleware]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src/clj"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.3.2"]
                                   [hawk "0.2.11"]
                                   [eftest "0.5.9"]
                                   [kerodon "0.9.1"]]}})
