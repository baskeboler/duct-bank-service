(ns bank-service.model.transactions.protocol)

(defprotocol Transactions
  (transfer! [db info])
  (deposit! [db info])
  (withdraw! [db info]))