(ns airsonic-ui.components.collection.views
  "A collection is a list of audio files that belong together (e.g. an album or
  a podcast's overview)"
  (:require [airsonic-ui.helpers :refer [format-duration]]
            [airsonic-ui.routes :as routes :refer [url-for]]
            [airsonic-ui.views.cover :refer [cover card]]
            [bulma.icon :refer [icon]]))

(defn collection-info [{:keys [songCount duration year]}]
  (vec (cond-> [:ul.is-smaller.collection-info
                [:li [icon :audio-spectrum] (str songCount (if (= 1 songCount)
                                                             " track" " tracks"))]
                [:li [icon :clock] (format-duration duration)]]
         year (conj [:li [icon :calendar] (str "Released in " year)]))))

(defn album-card [album]
  (let [{:keys [artist artistId name id]} album]
    [card album
     :url-fn #(url-for ::routes/album.detail {:id id})
     :content [:div
               ;; link to album
               [:div.title.is-5
                [:a {:href (url-for ::routes/album.detail {:id id})
                     :title name} name]]
               ;; link to artist page
               [:div.subtitle.is-6 [:a {:href (url-for ::routes/artist.detail {:id artistId})
                                        :title artist} artist]]]]))

(defn listing [albums]
  ;; always show 5 in a row
  [:div.columns.is-multiline.is-mobile
   (for [[idx album] (map-indexed vector albums)]
     ^{:key idx} [:div.column.is-one-fifth-desktop.is-one-quarter-tablet.is-half-mobile
                  [album-card album]])])

(defn detail
  "Lists all songs in an album"
  [{:keys [album]}]
  [:div
   [:section.hero.is-small>div.hero-body
    [:div.container
     [:article.collection-header.media
      [:div.media-left [cover album 128]]
      [:div.media-content
       [:h2.title (:name album)]
       [:h3.subtitle (:artist album)]
       [collection-info album]]]]]
   [:section.section>div.container [song/listing (:song album)]]])
