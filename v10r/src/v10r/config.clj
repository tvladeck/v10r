(ns v10r.config
  (:require [taoensso.carmine :as r]))

;;;;;;;;;;;;;;;;;;;;;;;REDIS CONFIGURATION;;;;;;;;;;;;;;;;;;;;;;;;;; 

(def pool (r/make-conn-pool :max-active 8)) 
(def spec-server1 (r/make-conn-spec   :host     "127.0.0.1"
                                      :port     6379))

(defmacro carmine
  "
  Opens a connection to Redis.
  Acts like (partial with-conn pool spec-server1).
  "
  [& body] `(r/with-conn pool spec-server1 ~@body))

;;;;;;;;;;;;;;;;;;;;;;;CONSTANTS;;;;;;;;;;;;;;;;;;;;;;;;;; 
(def SCENARIOS [1 10])

(def BASE 10)

(def ATOMS (Math/pow 2 BASE))

(def ALPHA (/ 0.1 (* ATOMS (Math/log ATOMS))))

(def INIT-BETA 1000)

(def INIT-TOTAL (/ INIT-BETA ALPHA))

(def INIT-INDIVIDUAL (/ INIT-TOTAL ATOMS))

(def NUMBER-MARKETS 100)

