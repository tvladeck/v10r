(ns v10r.utilities
  (:require [v10r.state :as state]
            [taoensso.carmine :as r])
  (:use v10r.config))

;;;;;;;;;;;;;;;;;;;;;;;NON APP HELPER FUNCTIONS;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-market
  [key no-events]
  (map #(carmine (r/hset key % 10)) (range 0 no-events)))

(defn create-markets
  [no-events no-markets]  (map #(create-market % no-events) (range 0 no-markets))
  (map #(create-market % no-events) (range 0 no-markets)))

(defn set-multi-start
  [no-markets start-value]
  (doseq
    [index (range 0 no-markets)]
    (carmine (r/hset (state/create-redis-market-keyword index) "START" start-value))))

