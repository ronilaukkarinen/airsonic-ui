(ns airsonic-ui.components.artist.views
  (:require [airsonic-ui.views.album :as album]))

(defn link-button [attrs children]
  [:p.control>a.button.is-small (merge attrs {:target "_blank"}) children])

(defn lastfm-link [artist-info]
  [link-button {:href (:lastFmUrl artist-info)} "See on last.fm"])

(defn musicbrainz-link [artist-info]
  (let [href (str "https://musicbrainz.org/artist/" (:musicBrainzId artist-info))]
    [link-button {:href href} "See on musicbrainz"]))

(defn detail
  "Creates a nice artist page displaying the artist's name, bio (if available and
  listing) their albums."
  [{:keys [artist artist-info]}]
  [:div
   [:section.hero>div.hero-body
    [:h2.title (:name artist)]
    [:div.content
     [:p {:dangerouslySetInnerHTML {:__html (:biography artist-info)}}]
     (when-not (empty? (select-keys artist-info [:lastFmUrl :musicBrainzId]))
       [:div.field.is-grouped
        (when (:lastFmUrl artist-info)
          [lastfm-link artist-info])
        (when (:musicBrainzId artist-info)
          [musicbrainz-link artist-info])])]]
   [:section.section [album/listing (:album artist)]]])
