# Google Authenticator [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Automate%20Google%20Authenticator%20using%20Clojure&url=https://github.com/ashwinbhaskar/Google-Authenticator)

A Clojure program designed to compute your google authenticator otp. I personally use this to automate connecting to VPNs along with apple script.
 But you could find other use cases where you would want to automate the computation and submission of the OTP.



## TOTP

Google authenticator uses Totp (Time based One Time Password) for 2 factor authentication. 
When you/your app registers with google authenticator, google gives you a shared secret key (could be in the form of QR code).
The shared key is a Base32Encoded String that looks like this `JBSWY3DPEHPK3PXP`

TOTP algorithm computes the Otp by doing a HMAC of the secret key and current time stamp (expressed as steps of
30 seconds from EPOCH). So, the OTP changes every 30 seconds. You can read more about the algorithm [here](https://tools.ietf.org/html/rfc6238).

## Usage (JVM)
This program takes in a file containing your secret key (base32 encoded) as an argument and an optional argument specifying the path of the file where the
otp will be written to. If the second argument is not specified it prints the otp to console.


```
make apply-patch
lein compile
lein uberjar
cd target
```

Since we are using cli-matic, we can see the ways to run this program by
giving

`java -jar target/google_authenticator-0.1.0-SNAPSHOT-standalone.jar p --help`

You should see an output similar to this
```
NAME:
 google-authenticator gen-otp - Generates google authenticator otp and prints it to the console

USAGE:
 google-authenticator [gen-otp|p] [command options] [arguments...]

OPTIONS:
   -sk, --secret-key S  Parameter A
   -?, --help
```

Example 1 - Print OTP on to the console
```
java -jar target/google_authenticator-0.1.0-SNAPSHOT-standalone.jar p --secret-key j4ok7qmclj23gwa336rrjrv123456789
```

Example 2 - Read secret key from a file and write otp to a file

```
java -jar target/google_authenticator-0.1.0-SNAPSHOT-standalone.jar o --secret-key-file-path ~/.secret_key --output-file ~/.otp
```

#Usage (GraalVM)

Please follow the instruction here to generate a native image - https://github.com/BrunoBonacci/graalvm-clojure/blob/master/doc/clojure-graalvm-native-binary.md

Once a native image is generated you can do the following (assuming the name of your native image is google_authenticator)

Example 1 - Print OTP on to the console

```
./google_authenticator p --secret-key j4ok7qmclj23gwa336rrjrv123456789
```

Example 2 - Read secret key from a file and write otp to a file

```
./google_authenticator o --secret-key-file-path ~/.secret_key --output-file ~/.otp
```

