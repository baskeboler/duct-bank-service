(ns bank-service.handler.transactions
  (:require [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]
            [honeysql-postgres.format]
            [honeysql.core :as sql]
            [ataraxy.response :as response]
            [duct.database.sql]
            [duct.logger :as logging]
            [bank-service.utils :refer [str->uuid]]))



(defn has-funds? [db account-id amount]
  (let [q (sql/format {:select [:balance]
                       :from   [:accounts]
                       :where  [:= :id (str->uuid account-id)]})
        account-balance (-> db
                            (jdbc/query q)
                            first
                            :balance)]
    (>= account-balance amount)))

(defn account-exists? [db account-id]
  (let [q (sql/format {:select [1]
                       :from [:accounts]
                       :where [:= :id account-id]})]
    (not (-> db
             (jdbc/query q)
             empty?))))

(defn create-txn
  ([db txn-type]
   (create-txn db txn-type nil nil))
  ([db txn-type user-id]
   (create-txn db txn-type user-id nil))
  ([db txn-type user-id comment]
   (assert (#{:transfer :deposit :withdrawal} txn-type))
   (let [q (sql/format {:insert-into  :transactions
                        :values  [{:transaction-type (name txn-type)
                                   :user-id          user-id
                                   :comment          comment}]})]
     (-> db
         (jdbc/execute! q {:return-keys true})
        ;; (first)
         :id))))

(defn create-transfer [db {:keys [from to amount user comment]}]

  (let [txnid (create-txn db :transfer user comment)
        q (sql/format {:insert-into :transfers
                       :values [{:id txnid
                                 :from-account-id (str->uuid from)
                                 :to-account-id   (str->uuid to)
                                 :amount amount}]})]
    (-> db
        (jdbc/execute! q {:return-keys true}))))

(defn create-deposit [db {:keys [account amount user comment]}]
  (let [txnid (create-txn db :deposit user comment)
        q (sql/format {:insert-into :deposits
                       :values [{:id txnid
                                 :account-id (str->uuid account)
                                 :amount amount}]})]
    (-> db
        (jdbc/execute! q {:return-keys true}))))

(defn create-withdrawal [db {:keys [account amount user comment]}]
  (let [txnid (create-txn db :withdrawal user comment)
        q (sql/format {:insert-into :withdrawals
                       :values [{:id txnid
                                 :account-id (str->uuid account)
                                 :amount amount}]})]
    (-> db
        (jdbc/execute! q {:return-keys true}))))


(comment
  (def test-accounts  (map :id (dev/q "select id from accounts")))
  (def acc1 (first test-accounts))
  (def acc2 (second test-accounts))
  )



(defn update-funds! [db account-id amount]
  (let [q ["update accounts set balance = balance + ? where id = ?" amount account-id]]
    (-> db
        (jdbc/execute! q))))

(defn do-transfer [db from to amount]
  (jdbc/with-db-transaction [conn db {:read-only? false}]

    (create-transfer conn {:from from :to to :amount amount :comment (str "transfer " amount " to " to)})
    (update-funds! conn from (- amount))
    (update-funds! conn to amount)

    {:status :success}))

(defn transfer!*
  [db {:keys [from to amount]}]
  (when-not (has-funds? db from amount)
    (throw (ex-info "Insufficient funds" {:status :error
                                          :error :insufficient-funds
                                          :account from
                                          :amount amount})))
  (when-not (account-exists? db to)
    (throw (ex-info "Account does not exist" {:status :error
                                              :error :account-does-not-exist
                                              :account to
                                              :amount amount})))
  (do-transfer db from to amount))


(defn deposit!*
  [db {:keys [to amount]}]
  (when-not (account-exists? db to)
    (throw (ex-info "Account does not exist" {:status :error
                                              :error :account-does-not-exist
                                              :account to
                                              :amount amount})))
  (jdbc/with-db-transaction [conn db {:read-only false}]
    (create-deposit conn {:account to
                          :amount amount})

    (update-funds! db to amount)
    {:status :success}))

(defn withdraw!*
  [db {:keys [account amount]}]

  (when-not (has-funds? db account amount)
    (throw (ex-info "Insufficient funds" {:status :error
                                          :error :insufficient-funds
                                          :account account
                                          :amount amount})))
  (when-not (account-exists? db account)
    (throw (ex-info "Account does not exist" {:status :error
                                              :error :account-does-not-exist
                                              :account account
                                              :amount amount})))
  (jdbc/with-db-transaction [conn db {:read-only false}]
    (create-withdrawal conn {:account account
                             :amount amount})

    (update-funds! db account (- amount))
    {:status :success}))

(defprotocol Transactions
  (transfer! [db info])
  (deposit! [db info])
  (withdraw! [db info]))

(extend-protocol Transactions
  duct.database.sql.Boundary
  (transfer! [{db :spec} {:keys [from to amount]}]
    (try
      [::response/ok (transfer!* db {:from   (str->uuid from)
                                     :to     (str->uuid to)
                                     :amount amount})]
      (catch clojure.lang.ExceptionInfo e
        [::response/internal-server-error (ex-data e)])))
  
  (deposit! [{db :spec} {:keys [to amount]}]
    (try
      [::response/ok (deposit!* db {:to     (str->uuid to)
                                    :amount amount})]
      (catch clojure.lang.ExceptionInfo e
        [::response/internal-server-error (ex-data e)])))
  
  (withdraw! [{db :spec} {:keys [from amount]}]
            (try
              [::response/ok (withdraw!*   db {:account   (str->uuid from)
                                               :amount amount})]
              (catch clojure.lang.ExceptionInfo e
                [::response/internal-server-error (ex-data e)]))))


(defmethod ig/init-key ::transfer [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger "transfer " info )
    (transfer! db info)))


(defmethod ig/init-key ::deposit [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger (format "deposit: %s" info))
    (deposit! db info)))

(defmethod ig/init-key ::withdraw [_ {:keys [db logger]}]
  (fn [{[_ info] :ataraxy/result}]
    (logging/info logger (format "withdrawal: %s" info))
    (withdraw! db info)))

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
  1000.0M)