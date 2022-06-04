(ns user
  (:require [duct.middleware.buddy :as b]))

(defn dev
  "Load and switch to the 'dev' namespace."
  []
  (require 'dev)
  (in-ns 'dev)
  :loaded)
