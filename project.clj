(defproject com.rentpath/structured-logging "0.1.1-SNAPSHOT"
  :description "Fork of puppetlabs/structured-logging, write data structures to your logs from clojure."
  :url "https://github.com/rentpath/structured-logging"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[net.logstash.logback/logstash-logback-encoder "4.2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [cheshire "5.6.3"]]
  :deploy-repositories [["releases" {:url "https://clojars.org/repo/"
                                     :username [:gpg :env/CLOJARS_USERNAME]
                                     :password [:gpg :env/CLOJARS_PASSWORD]
                                     :sign-releases false}]]
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.8.0"]
                                       [org.clojure/tools.logging "0.3.1"]
                                       [org.slf4j/slf4j-api "1.7.12"]]}})
