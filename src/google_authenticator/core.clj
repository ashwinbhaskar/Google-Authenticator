(ns google-authenticator.core
  (:import
    [org.apache.commons.codec.binary Base32]
    (javax.crypto Mac)
    (javax.crypto.spec SecretKeySpec))
  (:gen-class))


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
    (.toByteArray (biginteger ts))
    ))

(defn paddts-if-necessary
  "Takes in timestamp as byte array and pads it with 0 until its length is 8"
  [byte-array]
  (loop [acc byte-array]
    (if (= (count acc) 8)
      (into-array Byte/TYPE  acc)
      (recur (cons (byte 0) acc)))))

(defn paddotp-if-necessary
  "returns otp string with length 6. Pads with zero if length is not 6"
  [otp]
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

(defn -main [path-to-secret-key & args]
  (let [secret-key (apply str (remove #{\newline} (slurp path-to-secret-key)))
        otp (get-otp secret-key)]
    (if (empty? args)
      (println otp)
      (spit (nth args 0) otp))))