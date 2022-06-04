(ns bank-service.main
  (:require [duct.core :as duct]
            [bank-service.model.user.core]
            [bank-service.model.session.core]
            [bank-service.utils])
  (:gen-class))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys     (or (duct/parse-keys args) [:duct/daemon])
        profiles [:duct.profile/prod]]
    (-> (duct/resource "bank_service/config.edn")
        (duct/read-config)
        (duct/exec-config profiles keys))
    (System/exit 0)))
