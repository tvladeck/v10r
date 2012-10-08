(ns v10r.core
  (:use v10r.compute)
  (:require [clojure.core.reducers :as par])
  (:use v10r.config))

(defn -main
  "I don't do a whole lot."
  []
  (loop []
    (into []
          (par/map
               #(robust-compute-and-set-market SCENARIOS ALPHA %) 
               (range 0 NUMBER-MARKETS)))
    (recur)))
