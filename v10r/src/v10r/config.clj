(ns v10r.config)

;; REDIS configuration
(def pool (r/make-conn-pool :max-active 8))

(def spec-server1 (r/make-conn-spec :host     "127.0.0.1"
                                    :port     6379
                                    :timeout  4000))

(def scenarios [-10 10])

(def alpha (/ 0.02 (* 1024 (Math/log 1024))))

;;;;;;;;;;;;;;;;;;;;;;;NON APP HELPER FUNCTIONS;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-market
  [key no-events]
  (map #(carmine (r/hset key % (rand-int 100))) (range 0 no-events)))

(defn create-markets
  [no-events no-markets]
  (map #(create-market % no-events) (range 0 no-markets)))
