(ns airsonic-ui.components.podcast.subs
  (:require [re-frame.core :refer [reg-sub]]))

;; this unwraps the api response into a collection
(reg-sub
 ::podcast.response
 :<- [:api/response-for "getPodcasts"]
 :channel)

(defn podcast-channels
  "Given a podcast response, returns information about the channels that have
  been subscribed to."
  [response _]
  (map #(dissoc % :episode) response))

(reg-sub
 ::podcast.channels
 :<- [::podcast.response]
 podcast-channels)

(defn sorted-podcast-episodes
  "Given a podcast response, returns all episodes sorted by a given function"
  [response [_ key-fn & {:keys [n reverse?]
                         :or {n 15
                              reverse? true}}]]
  ;; some podcasts have an :artist and some don't, we make sure all of them have one
  (let [id->channel (into {} (map (juxt :id :title) response))]
    (let [sorted (->> (mapcat :episode response)
                      (map (fn [episode]
                             (assoc episode :artist (id->channel (:channelId episode)))))
                      (sort-by (or key-fn identity)))]
      (take n (if reverse? (reverse sorted) sorted)))))

(reg-sub
 ::podcast.episodes-by
 :<- [::podcast.response]
 sorted-podcast-episodes)
