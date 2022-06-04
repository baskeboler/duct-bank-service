(ns bank-service.handler.auth
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig]
            ;; [duct.middleware.buddy :as b]
            [duct.logger :as logging]
            [duct.database.sql]
            [bank-service.model.session.protocol :as session]
            [bank-service.model.user.core :as user]))

(defn- get-test-token-authfn
  [db logger]
  (fn [_ token]
    (logging/info logger (format "token: %s" token))
    (-> db
        (session/get-session-by-token token)
        :user-id)))

(defmethod ig/init-key ::auth-fn
  [_ {:keys [db logger]}]
  (get-test-token-authfn db logger))


(defmethod ig/init-key ::login [_ {:keys [db logger]}]
  (fn [{[_ login-info] :ataraxy/result}]
    (logging/info logger (format "logging in: %s" (str login-info)))
    (logging/info logger (format "db in: %s" (str db)))
    (let [{:keys [user password]} login-info
          the-user                (when user (user/get-user-by-name db user))]
      (if (some? the-user)
        (if (user/user-password-valid? db {:username user
                                           :password password})
          [::response/ok (session/fetch-or-create-session! db (:id the-user))]
          [::response/unauthorized "Invalid username or password"])
        [::response/not-found "user not found"]))))

(defmethod ig/init-key ::signup [_ {:keys [db logger]}]
  (fn [{[_ signup-info] :ataraxy/result}]
    (let [{user :user password :password} signup-info]
      
       [::response/ok {:example "data"}])))