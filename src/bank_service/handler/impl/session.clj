(ns bank-service.handler.impl.session
  (:require [bank-service.utils :refer [str->uuid]]
            [clojure.java.jdbc :as jdbc]
            [honeysql-postgres.format]
            [honeysql.core :as sql]))




(defn create-session [db user-id]
  (let [q (sql/format {:insert-into :user-sessions
                       :values [{:user-id (str->uuid user-id)}]})]
    (-> db
        (jdbc/execute! q {:return-keys true})
       ;; (first)
        :id)))

(defn get-session-by-id [db session-id]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:= :id (str->uuid session-id)]})]
    (-> db
        (jdbc/query q)
        first)))

(defn get-session-by-user-id [db user-id]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:and
                                [:= :user-id (str->uuid user-id)]
                                [:= :is-active true]]})]
    
    (-> db
        (jdbc/query q)
        first)))

(defn get-session-by-token [db token]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:and
                                [:= :token token]
                                [:= :is-active true]]})]
    (-> db
        (jdbc/query q)
        first)))

(defn deactivate-session [db session-id]
  (let [q (sql/format {:update :user-sessions
                       :set {:is-active false}
                       :where [:= :id (str->uuid session-id)]})]
    (-> db
        (jdbc/execute! q))))