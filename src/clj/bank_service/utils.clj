(ns bank-service.utils
  (:import [java.util UUID]))

(defn str->uuid [arg]
  (if (uuid? arg)
    arg
    (UUID/fromString arg)))
