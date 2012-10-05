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

(defn create-redis-key
  "
  creates the key that will serve to identify a market and a scenario
  in the redis store

  the format of the key is:

  M1S1

  where M1 = Market 1
  and S1 = Scenario 1
  "
  [market-id scenario-id]
  (string/join ["M" market-id "S" scenario-id]))

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


(defn get-total
  [market-id]
  (carmine (r/hvals (string/join ["m" market-id]))))

