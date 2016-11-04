(ns puppetlabs.structured-logging.core-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :refer [*logger-factory*]]
            [clojure.tools.logging.impl :as impl]
            [puppetlabs.structured-logging.core :refer :all]
            [puppetlabs.structured-logging.protocols :refer :all]))

(def ^:private stns *ns*)

(defn make-log-record [logger-ns level ex message marker]
  (letfn [(maybe-add-exception [rec]
            (if ex (assoc rec :throwable ex) rec))
          (maybe-add-marker [rec]
            (if marker
              (assoc rec :marker marker)
              rec))]
    (-> {:ns logger-ns, :level level, :message message}
        maybe-add-exception
        maybe-add-marker)))

(defn atom-logger-factory [log-atom]
  (reify impl/LoggerFactory
    (name [_] "atomLogger")
    (get-logger [_ logger-ns]
      (reify
        impl/Logger
        (enabled? [_ _] true)
        (write! [_ level ex message]
          (swap! log-atom conj (make-log-record logger-ns level ex message nil)))
        MarkerLogger
        (write-with-marker! [logger level ex message marker]
          (swap! log-atom conj (make-log-record logger-ns level ex message marker)))))))

(defn expect-log [f expected-log]
  (let [log (atom [])]
    (binding [*logger-factory* (atom-logger-factory log)]
      (f))
    (is (= expected-log @log))))

(def throwable (Exception. "ex"))

(deftest maplog-behavior
  (are [f expected] (expect-log f expected)
       #(maplog :error {:x 1} (fn [m] (str "Test " (:x m))))
       [{:ns stns, :level :error, :message "Test 1" :marker {:x 1}}]

       #(maplog [:sync :error] {:x 2} (fn [m] (str "Test " (:x m))))
       [{:ns "sync", :level :error, :message "Test 2" :marker {:x 2}}]

       #(maplog [:sync :error] throwable {:x 3} (fn [m] (str "Test " (:x m))))
       [{:ns "sync", :level :error, :message "Test 3", :throwable throwable
         :marker {:x 3}}]))
