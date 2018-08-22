(ns airsonic-ui.api.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn response-for
  [db [_ endpoint params]]
  (get-in db [:api/responses [endpoint params]]))

(reg-sub :api/response-for response-for)
