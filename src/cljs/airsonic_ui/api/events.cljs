(ns airsonic-ui.api.events
  "This namespace contains all events relevant to API interaction. It contains
  an event handler which issues requests as well as the appropriate handlers,
  which dispatch :notification events in case of errors."
  (:require [re-frame.core :refer [reg-event-fx]]
            [ajax.core :as ajax]
            [airsonic-ui.api.helpers :as api]))

(defn- cache-path [endpoint params] [:api/responses [endpoint params]])

(defn api-request
  "Event handler to issue API request; takes care of authorization based on our
  current app state."
  [{:keys [db]} [_ endpoint params]]
  {:http-xhrio {:method :get
                :uri (api/url (:credentials db) endpoint params)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:api/good-response endpoint params]
                :on-failure [:api/failed-response endpoint params]}
   :db (assoc-in db (conj (cache-path endpoint params) :api/is-loading?) true)})

(reg-event-fx :api/request api-request)

(defn good-api-response
  "Handles when the server responded. There could still be an error while
  processing the request on the server side which we have to account for."
  [fx [_ endpoint params response]]
  (let [response-cache (cons :db (cache-path endpoint params))]
    (try
      (assoc-in fx response-cache (api/unwrap-response response))
      (catch ExceptionInfo e
        {:dispatch [:notification/show :error (api/error-msg e)]
         :db (update-in fx response-cache dissoc :api/is-loading?)}))))

(reg-event-fx :api/good-response good-api-response)

(defn failed-api-response
  "Handler for catastrophic failures (network errors and such things)"
  [fx [ev endpoint params]]
  (let [response-cache (cons :db (cache-path endpoint params))]
    {:log ["API call gone bad; are CORS headers missing? check for :status 0" ev] ; <- the :log effect is registered in ../events.cljs
     :dispatch [:notification/show :error "Communication with server failed. Check browser logs for details."]
     :db (update-in fx response-cache dissoc :api/is-loading?)}))

(reg-event-fx :api/failed-response failed-api-response)
