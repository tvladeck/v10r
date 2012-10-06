(ns v10r.compute
  (:use v10r.config)
  (:require [incanter.core :as i])
  (:require [v10r.state :as state])
  (:require [clojure.core.reducers :as par]))

(defn compute-and-set-market
  "
  core function of the namespace. takes a vector of scenarios and a market-id. 
  it grabs the vector of event quantities from redis, computes the marginal addition to 
  sumexp in the price function of each atomic increase in each event quantity. 

  sends the vector of increases along with scenarios to state/set-event. 

  sends the current market sumexp to state/set-market-sum
  "
  [scenarios alpha market-id]
  (let [market            (state/get-market market-id)
        liq               (* alpha (i/sum market))
        exp-normed-market (i/exp (i/div market liq))
        exp-normed-scens  (i/exp (i/div scenarios liq))
        mapped-scens      (i/mmult exp-normed-market (i/trans exp-normed-scens))
        market-dummy      (i/trans (repeat (count scenarios) exp-normed-market))
        sumexp-diff       (i/minus mapped-scens market-dummy)
        market-sum        (i/sum exp-normed-market)
        num-scenarios     (count scenarios)]
    (state/set-market-sum market-id market-sum liq)
    (dorun
      (map
        (fn [index]
          (state/set-scenario (i/$ index sumexp-diff) index market-id))
        (range 0 num-scenarios)))))

(defn fuxing
  "
  core function of the namespace. takes a vector of scenarios and a market-id. 
  it grabs the vector of event quantities from redis, computes the marginal addition to 
  sumexp in the price function of each atomic increase in each event quantity. 

  sends the vector of increases along with scenarios to state/set-event. 

  sends the current market sumexp to state/set-market-sum
  "
  [scenarios alpha market-id]
  (let [market            (state/get-market market-id)
        liq               (* alpha (i/sum market))
        exp-normed-market (i/exp (i/div market liq))
        exp-normed-scens  (i/exp (i/div scenarios liq))
        mapped-scens      (i/mmult exp-normed-market (i/trans exp-normed-scens))
        market-dummy      (i/trans (repeat (count scenarios) exp-normed-market))
        sumexp-diff       (i/minus mapped-scens market-dummy)
        market-sum        (i/sum exp-normed-market)
        num-scenarios     (count scenarios)]
    
    sumexp-diff

    ))


