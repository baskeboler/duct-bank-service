(ns bank-service.handler.auth
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig]
            ;; [duct.middleware.buddy :as b]
            [bank-service.handler.impl.session :as session]
            [bank-service.handler.impl.user :as user]
            ))

(defn- get-test-token-authfn
  [db logger]
  (fn [_ token]
    (-> db
        (session/get-session-by-token token)
        :user-id)))

(defmethod ig/init-key ::auth-fn
  [_ {:keys [db logger]}]
  (get-test-token-authfn db logger))


(defmethod ig/init-key ::login 
  [_ {:keys [db logger]}]
  (fn [{[_ login-info] :ataraxy/result}]
    (let [{:keys [user password]} login-info
          ;; hashed (hashers/)
          ])
    [::response/ok {:example "data"}]))

(defmethod ig/init-key ::signup
  [_ {:keys [db logger]}]
  (fn [{[_ signup-info] :ataraxy/result}]
    [::response/ok {:example "data"}]))