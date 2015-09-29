(defproject puppetlabs/structured-logging "0.1.0"
  :description "Write data structures to your logs from clojure."
  :url "https://github.com/puppetlabs/structured-logging"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [net.logstash.logback/logstash-logback-encoder "4.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.match "0.2.2"]
                 [cheshire "5.4.0"]
                 [org.slf4j/slf4j-api "1.7.12"]]
  :plugins [[lein-release "1.0.5"]]
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]]
  :lein-release {:scm :git
                 :deploy-via :lein-deploy})
