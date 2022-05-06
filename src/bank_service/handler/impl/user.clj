(ns bank-service.handler.impl.user
  (:require [bank-service.utils :refer [str->uuid]]
            [clojure.java.jdbc :as jdbc]
            [honeysql-postgres.format]
            [honeysql.core :as sql]
            [buddy.hashers :as hashers]
            [bank-service.handler.impl.user :as user]))

(defn get-user-by-id [db user-id]
  (let [q (sql/format  {:select [:*]
                        :from   [:users]
                        :where  [:= :id (str->uuid user-id)]})]
    (-> db
        (jdbc/query q)
        first)))

(defn get-user-by-name [db name]
  (let [q (sql/format  {:select [:*]
                        :from   [:users]
                        :where  [:= :name name]})]
    (-> db
        (jdbc/query q)
        first)))
  

(defn encrypt-password [password] (hashers/derive password))
(defn valid-password? [pwd encrypted-pwd] (hashers/verify pwd encrypted-pwd))


(defn register-user! [db {:keys [username password email]}]
  (let [encrypted-password (encrypt-password password)
        q (sql/format {:insert-into :users
                       :values [{:name username
                                 :password encrypted-password
                                 :email email}]})]
    (-> db
        (jdbc/execute! q {:return-keys true})
        first)))

(defn user-password-authenticate [db {:keys [username password]}]
  (let [user (get-user-by-name db username)
        {encrypted-password :password} user]
    (valid-password? password encrypted-password)))