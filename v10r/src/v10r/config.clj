(ns v10r.config
  (:require [taoensso.carmine :as r]))

;; REDIS configuration
(def pool (r/make-conn-pool :max-active 8))

(def spec-server1 (r/make-conn-spec :host     "127.0.0.1"
                                    :port     6379))

(defmacro carmine
  "
  Opens a connection to Redis.
  Acts like (partial with-conn pool spec-server1).
  "
  [& body] `(r/with-conn pool spec-server1 ~@body))

(def SCENARIOS [-10 10])

(def BASE 10)

(def ATOMS (Math/pow 2 BASE))

(def ALPHA (/ 0.1 (* (Math/log ATOMS))))

(def INIT-BETA 1000)

(def INIT-TOTAL (/ INIT-BETA ALPHA))

(def INIT-INDIVIDUAL (/ INIT-TOTAL ATOMS))

;;;;;;;;;;;;;;;;;;;;;;;NON APP HELPER FUNCTIONS;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-market
  [key no-events]
  (map #(carmine (r/hset key % 10)) (range 0 no-events)))

(defn create-markets
  [no-events no-markets]  (map #(create-market % no-events) (range 0 no-markets))
  (map #(create-market % no-events) (range 0 no-markets)))

