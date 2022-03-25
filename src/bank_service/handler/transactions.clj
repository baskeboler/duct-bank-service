(ns bank-service.handler.transactions
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig]
            [duct.core :as duct]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql-postgres.format ]))


(defn str->uuid [uuid-str]
  (java.util.UUID/fromString uuid-str))

(defn has-funds? [db account-id amount]
  (let [q (sql/format {:select [:balance]
                        :from [:accounts]
                        :where [:= :id account-id]})
        account-balance (-> db
                            (jdbc/query q)
                            first
                            :balance)]
    (>= account-balance amount)))

(defn account-exists? [db account-id amount]
  (let [q (sql/format {:select [ 1]
                       :from [:accounts]
                       :where [:= :id account-id]})]
    (not (-> db
             (jdbc/query q)
             empty?))))

(defn update-funds! [db account-id amount]
  (let [q ["update accounts set balance = balance + ? where id = ?" amount account-id]]
    (-> db
        (jdbc/execute! q))))

(defn transfer!* [db {:keys [from to amount]}]
  (when-not (has-funds? db from amount)
    (throw (ex-info "Insufficient funds" {:status :error
                                          :error :insufficient-funds
                                          :account from
                                          :amount amount})))
  (when-not (account-exists? db to amount)
    (throw (ex-info "Account does not exist" {:status :error
                                              :error :account-does-not-exist
                                              :account to
                                              :amount amount})))
  (jdbc/with-db-transaction [conn db {:read-only? false}]
    (update-funds! conn from (- amount))
    (update-funds! conn to amount)
    {:status :success}))

(defprotocol Transactions 
  (transfer! [db info]))

(extend-protocol Transactions
 duct.database.sql.Boundary
  (transfer! [{db :spec} {:keys [from to amount]}]
    (try 
      [::response/ok (transfer!* db {:from   (str->uuid from)
                                     :to     (str->uuid to)
                                     :amount amount})]
      (catch clojure.lang.ExceptionInfo e
        [::response/internal-server-error (ex-data e)]))))

(defmethod ig/init-key ::transfer [_ {:keys [db]}]
  (fn [{[_ info] :ataraxy/result}]
    (transfer! db info)))

(comment 
  (let [acc1 (str->uuid "0f36bc09-433e-4b2c-8e2f-984cddfc27ba")
        acc2 (str->uuid "7e0402f4-b950-4260-bf97-47f35732bc5d")]
    (has-funds? (dev/db) acc1 1001)
    (transfer! (dev/db) {:from acc2
                         :to acc2
                         :amount 100}))
  (dev/q ["select * from accounts"])
  (->
   {:update  :accounts
    :set    {:balance   [[:balance :+ 1] :balance]}
    :where  [:= :id (str->uuid "0f36bc09-433e-4b2c-8e2f-984cddfc27ba")]}
   (sql/format {:pretty true :quoting :ansi}))
  1000.0M
  )