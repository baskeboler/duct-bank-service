(ns bank-service.handler.transactions
  (:require    [integrant.core :as ig]
               [honeysql-postgres.format]
               [duct.database.sql]
               [duct.logger :as logging]
               [bank-service.model.transactions.protocol :refer [deposit!
                                                                 transfer!
                                                                 withdraw!]]
               [bank-service.model.transactions.core]))


(defmethod ig/init-key ::transfer [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger "transfer " info)
    (transfer! db info)))


(defmethod ig/init-key ::deposit [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger (format "deposit: %s" info))
    (deposit! db info)))

(defmethod ig/init-key ::withdraw [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger (format "withdrawal: %s" info))
    (withdraw! db info)))