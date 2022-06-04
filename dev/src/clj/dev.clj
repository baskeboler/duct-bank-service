(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl :refer [auto-reset]]
            [eftest.runner :as eftest]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "bank_service/config.edn")))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src/clj" "src/clj" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

(defn db []
  (-> system (ig/find-derived-1 :duct.database/sql) val :spec))

(defn q [sql]
  (jdbc/query (db) sql))

(def migrations-folder "resources/migrations")

(defn new-migration []
  (let [now        (LocalDateTime/now)
        name       (.format now DateTimeFormatter/BASIC_ISO_DATE)
        file-names [(str migrations-folder "/" name ".up.sql")
                    (str migrations-folder "/" name ".down.sql")]]
    (doseq [f file-names
            :let [thefile (io/file f)]]
      (spit thefile ""))
    file-names))
