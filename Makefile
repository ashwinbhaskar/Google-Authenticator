SHELL := /usr/bin/env bash -e

apply-patch:
	javac ./java/src/lockfix/LockFix.java -cp ~/.m2/repository/org/clojure/clojure/1.10.0/clojure-1.10.0.jar
