(ns airsonic-ui.api.events
  "This namespace contains all events relevant to API interaction. It contains
  an event handler which issues requests as well as the appropriate handlers,
  which dispatch :notification events in case of errors."
  (:require [re-frame.core :refer [reg-event-fx]]
            [ajax.core :as ajax]
            [airsonic-ui.api.helpers :as api]))

(defn- api-url
  "Small helper function which makes constructing API URLs a bit easier"
  [db endpoint params]
  (let [creds (:credentials db)]
    (api/url (:server creds) endpoint (merge params (select-keys creds [:u :p])))))

(defn api-request
  "Event handler to issue API request; takes care of authorization based on our
  current app state."
  [{:keys [db]} [_ endpoint params]]
  {:http-xhrio {:method :get
                :uri (api-url db endpoint params)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:api/good-response]
                :on-failure [:api/bad-response]}})

(reg-event-fx :api/request api-request)

(defn good-api-response
  "Handles when the server responded. There could still be an error while
  processing the request on the server side which we have to account for."
  [fx [_ response]]
  (try
    (assoc-in fx [:db :response] (api/unwrap-response response))
    (catch ExceptionInfo e
      {:dispatch [:notification/show :error (api/error-msg e)]})))

(reg-event-fx :api/good-response good-api-response)

(defn bad-api-response
  "Handler for catastrophic failures (network errors and such things)"
  [db event]
  {:log ["API call gone bad; are CORS headers missing? check for :status 0" event]
   :dispatch [:notification/show :error "Communication with server failed. Check browser logs for details."]})

(reg-event-fx :api/bad-response bad-api-response)
