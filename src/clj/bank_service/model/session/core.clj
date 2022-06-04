(ns bank-service.model.session.core 
  (:require [bank-service.model.session.protocol :refer [Session]]
            [bank-service.utils :refer [str->uuid]]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]))




(defn create-session!* [db user-id]
  (let [q (sql/format {:insert-into :user-sessions
                       :values [{:user-id (str->uuid user-id)}]})]
    (-> db
        (jdbc/execute! q {:return-keys true})
       ;; (first)
        :id)))

(defn get-session-by-id* [db session-id]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:= :id (str->uuid session-id)]})]
    (-> db
        (jdbc/query q)
        first)))

(defn get-session-by-user-id* [db user-id]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:and
                                [:= :user-id (str->uuid user-id)]
                                [:= :is-active true]]})]

    (-> db
        (jdbc/query q)
        first)))

(defn get-session-by-token* [db token]
  (let [q (sql/format {:select [:*]
                       :from   [:user-sessions]
                       :where  [:and
                                [:= :token token]
                                [:= :is-active true]]})]
    (-> db
        (jdbc/query q)
        first)))

(defn fetch-or-create-session!* [db user-id]
  (let [session (get-session-by-user-id* db user-id)]
    (-> (if (some? session)
          session
          (create-session!* db user-id))
        (select-keys [:token :user-id]))))

(defn deactivate-session [db session-id]
  (let [q (sql/format {:update :user-sessions
                       :set {:is-active false}
                       :where [:= :id (str->uuid session-id)]})]
    (-> db
        (jdbc/execute! q))))


(extend-protocol Session
  duct.database.sql.Boundary
  (fetch-or-create-session! [{db :spec} user-id]
    (fetch-or-create-session!* db user-id))
  (get-session-by-id [{db :spec} session-id]
    (get-session-by-id* db session-id))
  (get-session-by-token [{db :spec} token]
    (get-session-by-token* db token))
  (create-session! [{db :spec} user-id]
    (create-session!* db user-id)))