(ns google-authenticator.core
  (:require [cli-matic.core :refer [run-cmd]])
  (:import
    [org.apache.commons.codec.binary Base32]
    (javax.crypto Mac)
    (javax.crypto.spec SecretKeySpec)
    (lockfix LockFix))
  (:gen-class))


(defmacro locking*                                          ;; patched version of clojure.core/locking to workaround GraalVM unbalanced monitor issue
  "Executes exprs in an implicit do, while holding the monitor of x.
  Will release the monitor of x in all circumstances."
  {:added "1.0"}
  [x & body]
  `(let [lockee# ~x]
     (LockFix/lock lockee# (^{:once true} fn* [] ~@body))))

(defn dynaload ;; patched version of clojure.spec.gen.alpha/dynaload to use patched locking macro
  [s]
  (let [ns (namespace s)]
    (assert ns)
    (locking* #'clojure.spec.gen.alpha/dynalock
              (require (symbol ns)))
    (let [v (resolve s)]
      (if v
        @v
        (throw (RuntimeException. (str "Var " s " is not on the classpath")))))))

(alter-var-root #'clojure.spec.gen.alpha/dynaload (constantly dynaload))

(defn decode-base32 [base32str]
  (.decode (new Base32) base32str))

(defn hmac
  [key-bytes data-bytes]
  (let [hmac-sha1 "HmacSHA1"
        signing-key (SecretKeySpec. key-bytes hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
    (.doFinal mac data-bytes)))



(defn totp-timestamp
  "Returns current unix time stamp in steps of 30 seconds from epoch time"
  []
  (let [ts (quot (System/currentTimeMillis)
                 (* 30 1000))]
    (.toByteArray (biginteger ts))))

(defn paddts-if-necessary
  "Takes in timestamp as byte array and pads it with 0 until its length is 8"
  [byte-array]
  (loop [acc byte-array]
    (if (= (count acc) 8)
      (into-array Byte/TYPE  acc)
      (recur (cons (byte 0) acc)))))

(defn paddotp-if-necessary
  "returns otp string with length 6. Pads with zero if length is not 6"
  [^Object otp]
  (loop [acc (.toString otp)]
    (if (= (count acc) 6)
      acc
      (recur (str "0" acc)))))

(defn get-hmac [secret-key] (hmac (decode-base32 secret-key)
                        (paddts-if-necessary (totp-timestamp))))

(defn get-truncated-hash
  "Does a truncation of the generated hmac hash as specified in rfc 6238
  https://tools.ietf.org/html/rfc6238"
  [hmac-hash]
  (let [offset (bit-and (nth hmac-hash 19) 15)]
    (bit-or (bit-shift-left (bit-and (nth hmac-hash offset) 127) 24)
            (bit-shift-left (bit-and (nth hmac-hash (+ offset 1)) 255) 16)
            (bit-shift-left (bit-and (nth hmac-hash (+ offset 2)) 255) 8)
            (bit-and (nth hmac-hash (+ offset 3)) 255))))

(defn get-otp [secret-key]
  (let [hmac-hash (get-hmac secret-key)
        truncated-hash (get-truncated-hash hmac-hash)]
    (paddotp-if-necessary (int
                            (mod truncated-hash
                                 (Math/pow 10 6))))))

(defn gen-otp-and-write-to-file [{:keys [secret-key-file-path output-file]}]
  (let [secret-key (apply str (remove #{\newline} (slurp secret-key-file-path)))
        otp (get-otp secret-key)]
    (spit output-file otp)))

(defn print-otp [{:keys [secret-key]}]
  (println (get-otp secret-key)))


(def CONFIGURATION
  {:app         {:command     "google-authenticator"
                 :description "A command-line to generate your google authenticator OTP"
                 :version     "0.1"}
   :global-opts [{:option  "base"
                  :as      "The number base for output"
                  :type    :int
                  :default 10}]
   :commands    [{:command     "gen-otp-to-file" :short "o"
                  :description ["Reads secret key from a file, "
                                "Generates google authenticator otp and writes it to a file"
                                "Looks great, doesn't it?"]
                  :opts        [{:option "secret-key-file-path" :short "skf" :as "Second addendum" :type :string}
                                {:option "output-file" :short "opf" :env "AA" :as "First addendum" :type :string}]
                  :runs        gen-otp-and-write-to-file}
                 {:command     "gen-otp" :short "p"
                  :description "Generates google authenticator otp and prints it to the console"
                  :opts        [{:option "secret-key" :short "sk" :as "Parameter A" :type :string}]
                  :runs        print-otp}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args CONFIGURATION))

(comment
  (gen-otp-and-write-to-file {:secret-key-file-path "/Users/ashwinbhaskar/.secret_key" :output-file "/Users/ashwinbhaskar/.otp"})
  (print-otp {:secret-key "j4ok7qmclj23gwa336rrjrv254usaeig"}))
