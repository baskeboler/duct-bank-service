(ns bank-service.handler.auth-fn
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig]))

(defn- test-token-authfn [_ token] token)

(defmethod ig/init-key :bank-service.handler/auth-fn
  [_ options]
  test-token-authfn)

