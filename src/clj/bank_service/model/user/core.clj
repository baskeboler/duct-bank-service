(ns bank-service.model.user.core 
  (:require

   [clojure.java.jdbc :as jdbc]
   [buddy.hashers :as hashers]
   [honeysql.core :as sql]
   [duct.database.sql]
   [bank-service.utils :refer [str->uuid]]
   [bank-service.model.user.protocol :refer [User get-user-by-name]]))

(defn get-user-by-id* [db user-id]
  (let [q (sql/format  {:select [:*]
                        :from   [:users]
                        :where  [:= :id (str->uuid user-id)]})]
    (-> db
        (jdbc/query q)
        first)))

(defn get-user-by-name* [db username]
  (let [q (sql/format  {:select [:*]
                        :from   [:users]
                        :where  [:= :name username]})]
    (-> db
        (jdbc/query q)
        first)))


(defn encrypt-password [password] (hashers/derive password))
(defn valid-password? [pwd encrypted-pwd] (hashers/verify pwd encrypted-pwd))


(defn register-user!* [db {:keys [username password email]}]
  (let [encrypted-password (encrypt-password password)
        q (sql/format {:insert-into :users
                       :values [{:name username
                                 :password encrypted-password
                                 :email email}]})]
    (-> db
        (jdbc/execute! q {:return-keys true})
        first)))

(defn user-password-valid?* [db {:keys [username password]}]
  (let [{encrypted-password :password} (get-user-by-name* db username)]
    (valid-password? password encrypted-password)))



(extend-protocol User
  duct.database.sql.Boundary
  (register-user! [{db :spec} register-info]
    (register-user!* db register-info))
  (user-password-valid? [{db :spec} login-info]
    (user-password-valid?* db login-info))
  (get-user-by-id [{db :spec} user-id]
    (get-user-by-id* db user-id))
  (get-user-by-name [{db :spec} name]
    (get-user-by-name* db name)))
