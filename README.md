# Google Authenticator

A Clojure program designed to compute your google authenticator otp. I personally use this to automate connecting to VPNs along with apple script.
 But you could find other use cases where you would want to automate the computation and submission of the OTP.



## TOTP

Google authenticator uses Totp (Time based One Time Password) for 2 factor authentication. 
When you/your app registers with google authenticator, google gives you a shared secret key (could be in the form of QR code).
The shared key is a Base32Encoded String that looks like this `JBSWY3DPEHPK3PXP`

TOTP algorithm computes the Otp by doing a HMAC of the secret key and current time stamp (expressed as steps of
30 seconds from EPOCH). So, the OTP changes every 30 seconds. You can read more about the algorithm [here](https://tools.ietf.org/html/rfc6238).

## Usage
This program takes in a file containing your secret key (base32 encoded) as an argument and an optional argument specifying the path of the file where the
otp will be written to. If the second argument is not specified it prints the otp to console.


```
lein compile
lein uberjar
cd target
java -jar <generated-jar-file-name> "<file-path-to-secret-key?" "<optional-file-path-where-otp-will be written"
```
