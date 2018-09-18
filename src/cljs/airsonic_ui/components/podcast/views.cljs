(ns airsonic-ui.components.podcast.views
  (:require [re-frame.core :refer [subscribe]]
            [airsonic-ui.helpers :refer [muted-dispatch]]
            [airsonic-ui.routes :as routes :refer [url-for]]
            [airsonic-ui.components.podcast.subs :as subs]
            [airsonic-ui.views.cover :refer [card]]))

;; TODO: Actually play single episodes
;; TODO: Implement detail pages for podcasts
;; TODO: Implement CRUD frontend for podcasts

(defn channel-card
  "Displays the cover of a podcast and links to the podcasts detail page"
  [channel]
  [card channel
   :url-fn #(url-for ::routes/podcast.detail {:id (:id channel)})
   :content [:div.title.is-5
             [:a {:href (url-for ::routes/podcast.detail {:id (:id channel)})
                  :title (:title channel)} (:title channel)]]])
(defn- channel-overview [channels]
  [:div.columns.is-multiline.is-mobile
   (for [[idx channel] (map-indexed vector channels)]
     ^{:key idx}
     [:div.column.is-one-fifth-desktop.is-one-quarter-tablet.is-half-mobile
      [channel-card channel]])])

(defn- episode-list [episodes]
  [:table.table.is-striped.is-hoverable.is-fullwidth>tbody
   (for [[idx episode] (map-indexed vector episodes)]
     ^{:key idx}
     [:tr
      [:td.grow [:span
                 [:a {:href (url-for ::routes/podcast.detail {:id (:channelId episode)})}
                  (:artist episode)]
                 " - "
                 [:a {:title (:title episode)
                      :href "#"
                      :on-click (muted-dispatch [:audio-player/enqueue-next episode])}
                     (:title episode)]]]
      [:td>a {:title "Play next"
              :href "#"
              :on-click (muted-dispatch [:audio-player/enqueue-next episode])}]
      [:td>a {:title "Play last"
              :href "#"
              :on-click (muted-dispatch [:audio-player/enqueue-last episode])}]])])

(defn overview [_]
  (let [channels @(subscribe [::subs/podcast.channels])
        episodes @(subscribe [::subs/podcast.episodes-by :created])]
    [:section.section>div.container
     [:h1.title "Subscriptions"]
     [channel-overview channels]
     [:h1.title "Latest Episodes"]
     [episode-list episodes]]))
