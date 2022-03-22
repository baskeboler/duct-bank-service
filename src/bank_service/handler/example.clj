(ns bank-service.handler.example
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response] 
            [integrant.core :as ig]))

(defmethod ig/init-key :bank-service.handler/example [_ options]
  (fn [{[_] :ataraxy/result}]
    [::response/ok {:example "data"}]))
