(ns bank-service.model.user.protocol)

(defprotocol User
  (register-user! [db user-info])
  (user-password-valid? [db login-info])
  (get-user-by-id [db user-id])
  (get-user-by-name [db name]))
