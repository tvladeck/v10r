(ns v10r.utilities
  (:require [v10r.state :as state]))

(defn rebalance-market
  [market-id init]
  (let [market     (state/get-market market-id)
        least      (reduce min market)
        start      (+ init start)]
    (do
      (state/set-start market-id start)
      (doseq
        [index (range 0 ATOMS)]
        (state/increment market-id index (- least))))))

;;;;;;;;;;;;;;;;;;;;;;;NON APP HELPER FUNCTIONS;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-market
  [key no-events]
  (map #(carmine (r/hset key % 10)) (range 0 no-events)))

(defn create-markets
  [no-events no-markets]  (map #(create-market % no-events) (range 0 no-markets))
  (map #(create-market % no-events) (range 0 no-markets)))

