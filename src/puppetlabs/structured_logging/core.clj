(ns puppetlabs.structured-logging.core
  (:require [clojure.tools.logging :refer [*logger-factory*]]
            [clojure.tools.logging.impl :as impl]
            [puppetlabs.structured-logging.protocols :refer :all]
            [cheshire.core :as cheshire])
  (:import [net.logstash.logback.marker Markers LogstashMarker]
           [com.fasterxml.jackson.core JsonGenerator]))

(definterface ISemlogMarker
  (semlogMap [] "Returns the semlog map for this marker."))

(defn- merge-clojure-map-marker
  "Create a marker that, when written to the LogStash json encoder, will
  json-encode the given map `m` and merge it with any already-created json.

  Use the following encoder configuration inside your logback appender
  configuration to write your log messages as json:

    <encoder class=\"net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder\">
      <providers>
        <timestamp/>
        <message/>
        <loggerName/>
        <threadName/>
        <logLevel/>
        <logLevelValue/>
        <stackTrace/>
        <logstashMarkers/>
      </providers>
    </encoder>
  "
  [m]
  (proxy [LogstashMarker ISemlogMarker] ["SEMLOG_MAP"]
    (writeTo [^JsonGenerator generator]
      (binding [cheshire/*generator* generator]
        ;; `::none` is the 'wholeness' parameter to cheshire, which indicates which
        ;; start- and end-object markers to write. In this case we don't want any
        ;; of them, so we'll pass `::none`. This is not on the list of supported values,
        ;; but the fact that it's not equal to any of them means it will work.
        (cheshire/write m ::none)))
    (semlogMap [] m)))

(extend-protocol MarkerLogger
  org.slf4j.Logger
  (write-with-marker! [logger level e msg marker-map]
    (let [^String msg (str msg)
          marker (merge-clojure-map-marker marker-map)]
      (if e
        (case level
          :trace (.trace logger marker msg e)
          :debug (.debug logger marker msg e)
          :info  (.info  logger marker msg e)
          :warn  (.warn  logger marker msg e)
          :error (.error logger marker msg e)
          :fatal (.error logger marker msg e)
          (throw (IllegalArgumentException. (str level))))
        (case level
          :trace (.trace logger marker msg)
          :debug (.debug logger marker msg)
          :info  (.info  logger marker msg)
          :warn  (.warn  logger marker msg)
          :error (.error logger marker msg)
          :fatal (.error logger marker msg)
          (throw (IllegalArgumentException. (str level))))))))

(defmacro maplog
  "Logs an event with ctx-map as an slf4j event Marker.
  The logger-level parameter may be either a log level keyword like
  :error or a vector of a custom logger and the log level, like
  [:sync :error].  Calls (create-message ctx-map) to generate the
  log message string.

  Examples:

  (maplog :info {:status 200} #(str \"Received success status \" (:status %))

  (maplog [:sync :warn] {:remote ..., :response ...}
          #(format \"Failed to pull record from remote %s. Response: status %s\"
                   (:remote %) (:status %)))

  (maplog [:sync :info] {:remote ...}
          #(format \"Finished pull from %s in %s seconds\"
                   sync-time (:remote %)))

  (maplog :info {:status 200}
          #(i18n/trs \"Received success status {0}\" (:status %1)))"
  ([logger-level ctx-map create-message]
   `(maplog ~logger-level nil ~ctx-map ~create-message))
  ([logger-level throwable ctx-map create-message]
   `(let [logger-level# ~logger-level
          [ns# level#] (if (coll? logger-level#)
                         logger-level#
                         [~*ns* logger-level#])
          ns# (if (keyword? ns#) (name ns#) ns#)
          logger# (impl/get-logger *logger-factory* ns#)
          ctx-map# ~ctx-map]
      (when (impl/enabled? logger# level#)
        (write-with-marker! logger# level#
                            ~throwable
                            (~create-message ctx-map#)
                            ctx-map#)))))

(defmacro trace
  "Trace level logging."
  ([ctx-map create-message]
   `(trace nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :trace ~throwable ~ctx-map ~create-message)))

(defmacro debug
  "Debug level logging."
  ([ctx-map create-message]
   `(debug nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :debug ~throwable ~ctx-map ~create-message)))

(defmacro info
  "Info level logging."
  ([ctx-map create-message]
   `(info nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :info ~throwable ~ctx-map ~create-message)))

(defmacro warn
  "Warn level logging."
  ([ctx-map create-message]
   `(warn nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :warn ~throwable ~ctx-map ~create-message)))

(defmacro error
  "Error level logging."
  ([ctx-map create-message]
   `(error nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :error ~throwable ~ctx-map ~create-message)))

(defmacro fatal
  "Fatal level logging."
  ([ctx-map create-message]
   `(fatal nil ~ctx-map ~create-message))
  ([throwable ctx-map create-message]
   `(maplog :fatal ~throwable ~ctx-map ~create-message)))
