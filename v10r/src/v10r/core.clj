(ns v10r.core
  (:use v10r.compute)
  (:use v10r.state)
  (:require [clojure.core.reducers :as par])
  (:use v10r.config))

(defn -main
  "
  An infinite loop that runs 'robust-compute-and-set-market' on all 
  market-ids with the constants SCENARIOS and ALPHA as inputs. 

  There is no error handling at this level. Keeps running and if there is an 
  error, this gets handled by 'robust-compute-and-set-market'

  After each cycle, sends the message 'full-cycle' to the redis channel 'cycles'
  "
  []
  (loop []
    (do
      (into []
            (par/map
                 #(robust-compute-and-set-market SCENARIOS ALPHA %) 
                 (range 0 NUMBER-MARKETS)))
    (send-message "cycles" "full-cycle")
    (recur))))
