(ns bank-service.handler.example-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [ring.mock.request :as mock]))

(deftest smoke-test
  (testing "example page exists"
    (let [handler  (ig/init-key :bank-service.handler/example {})
          response (handler (mock/request :get "/example"))]
      (is (= :ataraxy.response/ok (first response)) "response ok"))))
