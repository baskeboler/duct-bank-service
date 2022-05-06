(ns bank-service.utils)

(defn str->uuid [arg]
  (if (uuid? arg)
    arg
    (java.util.UUID/fromString arg)))
