(ns airsonic-ui.components.collection.views
  (:require [airsonic-ui.views.song :as song]))

(defn detail
  "Lists all songs in an album"
  [{:keys [album]}]
  [:div
   [:section.hero>div.hero-body
    [:h2.title (:name album)]
    [:h3.subtitle (:artist album)]]
   [:section.section [song/listing (:song album)]]])
