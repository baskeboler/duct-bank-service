(ns bank-service.model.session.protocol)


(defprotocol Session
  (fetch-or-create-session! [db user-id])
  (get-session-by-id [db session-id])
  (get-session-by-token [db token])
  (create-session [db user-id]))

