(ns v10r.state
  (:require [taoensso.carmine :as r])
  (:require [clojure.string :as string])
  (:use v10r.config))


(defmacro carmine
  "
  Opens a connection to Redis.
  Acts like (partial with-conn pool spec-server1).
  "
  [& body] `(r/with-conn pool spec-server1 ~@body))

(defn set-market-sum
  "
  takes a market-id and sets the initial sum and liquidity of the market
  "
  [market-id sum liquidity]
  (carmine
    (r/hmset (string/join ["m" market-id]) "sum" sum "liquidity" liquidity)))

(defn get-market
  "
  takes a market-id and returns a vector of event quantities within the market
  "
  [^Integer key]
  (carmine (r/hvals key)))

(defn set-scenario
  "
  core function of the namespace. takes a vector of the atomic impacts of a single position
  movement scenario, along with an integer representing the scenario, and a market id, and sets
  the scenario in redis with the following format

  HMSET M1S1 0 atomic-increase 1 atomic-increase ...

  where M1S1 = Market 1, Scenario 1, 
  the 0, 1, 2, ... are the fields representing positions in the vector, and
  the atomic-increases are the scenario-specific atomic increases to sumexp
  "
  [atomic-increase-vector scenario market-id]
  (carmine
    (apply r/hmset)
      (create-redis-key market-id scenario) 
      (flatten
        (map-indexed
          (fn [index item] (vector index item) 
          vector)))))

(defn get-event
  [market-id event-id]
  (carmine (r/hvals (create-delta-keyword market-id event-id))))

(defn get-total
  [market-id]
  (carmine (r/hvals (string/join ["m" market-id]))))

;; Deprecated
;;
(defn create-delta-keyword
  "
  function to create a unique ID for all the logsumexp scenarios recorded by the compute namespace.
  given a market k and an event j returns the following
  (string m k e j)
  "
  [^Integer market ^Integer event]
  (string/join ["m" market "e" event]))

(defn set-event
  "
  core function to set a unique market x event in the redis store. given a market-id k, stores the event in redis with the following format

  " 
  [market-id event-id event-vec]
  (carmine
    (apply r/hmset
      (create-delta-keyword market-id event-id)
      event-vec)))

