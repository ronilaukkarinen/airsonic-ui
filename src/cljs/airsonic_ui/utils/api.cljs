(ns airsonic-ui.utils.api
  (:require [clojure.string :as str]
            [airsonic-ui.config :as config]))

(def default-params {:f "json"
                     :c "airsonic-ui-cljs"
                     :v "1.15.0"})

(defn- encode [c]
  (js/encodeURIComponent c))

(defn url
  "Returns an absolute url to an API endpoint"
  [server endpoint params]
  (let [query (->> (merge default-params params)
                   (map (fn [[k v]] (str (encode (name k)) "=" (encode v))))
                   (str/join "&"))]
    (str server (when-not (str/ends-with? server "/") "/") "rest/" endpoint "?" query)))

(defn song-url [server credentials song]
  (url server "stream" (merge (select-keys song [:id]) credentials)))

(defn cover-url [server credentials item size]
  (url server "getCoverArt" (merge {:id (:coverArt item) :size size} credentials)))

(defn is-error? [response]
  (= "failed" (get-in response [:subsonic-response :status])))

(defn- unwrap-response* [response]
  (-> (:subsonic-response response)
      (dissoc :status :version)
      vals
      first))

(defn ->exception
  "Takes an erroneous response and makes it a real exception"
  [response]
  (let [error (unwrap-response* response)]
    (ex-info (:message response) error)))

(defn unwrap-response
  "Retrieves the actual response body"
  [response]
  (if (is-error? response)
    (let [error (:error response)]
      (throw (->exception response)))
    (unwrap-response* response)))

(defn error-msg
  [exception-info]
  (let [{:keys [code message]} (ex-data exception-info)]
    (str "Error " code ": " message)))
