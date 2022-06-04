(ns bank-service.middleware.core
  (:require [ring.middleware.defaults :as defaults]
            [integrant.core :as ig]
            [duct.logger :as logging]))

(defn wrap-site [handler]
  (-> handler
      (defaults/wrap-defaults defaults/site-defaults)))

(defn wrap-api [handler] 
  (-> handler 
      (defaults/wrap-defaults defaults/api-defaults)))


(defmethod ig/init-key ::wrap-site [_ opts] wrap-site)
(defmethod ig/init-key ::api-middleware
  [_ {:keys [wrap-api buddy-authenticate buddy-authorize logger]}]

  (logging/warn logger "setting up middleware key ")

  (fn [handler]
    (logging/warn logger "applying middleware to handler")

    (reduce comp
            handler
            [buddy-authenticate
             buddy-authorize
             wrap-api])))


(defmethod ig/init-key ::wrap-api [_ opts] wrap-api)