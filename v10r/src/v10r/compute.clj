(ns v10r.compute
  (:use v10r.config)
  (:require [incanter.core :as i])
  (:require [v10r.state :as state]))

(defn- compute-and-set-market
  "
  core function of the namespace. takes a vector of scenarios and a market-id. 
  it grabs the vector of event quantities from redis, computes the marginal addition to 
  sumexp in the price function of each atomic increase in each event quantity. 

  sends the vector of increases for each scenario to state/set-scenario. 

  sends the current market sumexp and beta to state/set-market-sum

  calculation works as follows

  DELTA = INIT-INDIVIDUAL + beta * log sum_i exp (q_i + d_i) / beta - INIT-INDVIDUAL + beta * log sum_i exp q_i / beta
        = beta * log sum_i (exp q_i / beta) * (exp d_i / beta) - beta * log sum_i exp q_i / beta
        = beta * log sum_i (exp q_i / beta) + sumexp-diff - beta * log sum_i exp q_i / beta

  sumexp-diff = (exp q_i / beta) * (exp d_i / beta) - exp q_i / beta
  market-sum = log sum_i exp q_i / beta

  Finally, if the function completes, it sets the market's status as 'OK'
  "
  [scenarios alpha market-id]
  (let [market            (state/get-market market-id)
        beta              (* alpha (+ INIT-TOTAL (i/sum market)))
        exp-normed-market (i/exp (i/div market beta))
        exp-normed-scens  (i/exp (i/div scenarios beta))
        mapped-scens      (i/mmult exp-normed-market (i/trans exp-normed-scens))
        market-dummy      (i/trans (repeat (count scenarios) exp-normed-market))
        sumexp-diff       (i/minus mapped-scens market-dummy)
        market-sum        (i/sum exp-normed-market)
        num-scenarios     (count scenarios)]
    (do
      (state/set-market-sum market-id market-sum beta)
      (doseq
        [index (range 0 num-scenarios)]
        (state/set-scenario (i/$ index sumexp-diff) index market-id))
      (state/set-status-ok market-id))))

(defn robust-compute-and-set-market
  "
  only public function of the namespace.

  calls compute-and-set-market, and catches ANY error in the completion of the function, no
  matter the type of exception. 

  if there is no error, robust-compute-and-set-market does nothing additional to 
  compute-and-set-market. if there is an error, it will set the market's status in redis to
  'error'
  "
  [scenarios alpha market-id]
  (do
    (try (compute-and-set-market scenarios alpha market-id)
      (catch Exception exception-name (state/set-status-error market-id)))
    (state/send-message "cycles" (.toString (java.util.Date.)))))


