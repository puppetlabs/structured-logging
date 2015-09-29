(ns puppetlabs.structured-logging.protocols)

(defprotocol MarkerLogger
  (write-with-marker! [^org.slf4j.Logger logger level ^Throwable e msg marker]))
