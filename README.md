# Google Authenticator

A Clojure library designed to compute your google authenticator otp. You need to run the program
supplying the path to the file where your secret key is stored. Optionally you can specify the path to a 
file where the OTP will be written else OTP will be written to console output

## Usage

```
lein compile
lein uberjar
cd target
java -jar <generated-jar-file-name> "<file-path-to-secret-key?" "<optional-file-path-where-otp-will be written"
```