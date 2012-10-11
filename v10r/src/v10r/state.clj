(ns v10r.state
  (:require [taoensso.carmine :as r])
  (:require [clojure.string :as string])
  (:use v10r.config))

(defn create-redis-market-keyword
  "
  creates the id used to store sum, start, and beta values in redis
  "
  [market-id]
  (string/join ["M" market-id]))

(defn set-market-sum
  "
  takes a market-id and sets the startial sum and liquidity of the market
  "
  [market-id sum beta]
  (carmine
    (r/hmset (create-redis-market-keyword market-id) "SUM" sum "BETA" beta)))

(defn get-market
  "
  takes a market-id and returns an ordered sequence of event quantities within the market

  Importantly, preserves ordering, which is why HMGET is applied to a sequence
  of position IDs, as opposed to just using HVALS, which does not preserve ordering
  "
  [market-id]
  (map #(Double. %)
       (carmine (apply
                    r/hmget
                      market-id
                        (range 0 ATOMS)))))

(defn increment
  [market-id pos-id incr]
  (carmine (r/hincrby pos-id incr)))

(defn get-start
  "
  gets the 'start' value of the market, which is used to rebalance the market.
  "
  [market-id]
  (Double.
    (carmine
      (r/hget (create-redis-market-keyword market-id) "START"))))

(defn create-redis-scenario-keyword
  "
  creates the keyword that will serve to identify an markets price vector and a scenario
  in the redis store

  the format of the market-id is:

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
    (apply r/hmset
      (create-redis-scenario-keyword market-id scenario) 
      (flatten
        (map-indexed
          (fn [index item] (vector index item))
          atomic-increase-vector)))))

(defn set-status-error
  "
  sets a value in Redis that a market has failed to compute. 
  used to signal that the market should not be used until it is repaired
  "
  [market-id error-message]
  (carmine (r/hset "Status" market-id (string/join ["error:" error-message]))))

(defn set-status-ok
  "
  sets a value in Redis that a market has computed successfully 
  used to signal that the market is OK to use. 
  "
  [market-id]
  (carmine (r/hset "Status" market-id "OK")))

(defn send-message
  "
  Just a wrapper around redis' PUBLISH
  "
  [channel message]
  (carmine (r/publish channel message)))


