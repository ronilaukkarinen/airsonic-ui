(ns airsonic-ui.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [airsonic-ui.routes :as routes :refer [url-for]]
            [airsonic-ui.events :as events]
            [airsonic-ui.subs :as subs]

            [airsonic-ui.views.notifications :refer [notification-list]]
            [airsonic-ui.views.breadcrumbs :refer [breadcrumbs]]
            [airsonic-ui.views.bottom-bar :refer [bottom-bar]]
            [airsonic-ui.views.login :refer [login-form]]
            [airsonic-ui.views.album :as album]
            [airsonic-ui.views.song :as song]))

;; TODO: Find better names and places for these.

(defn album-detail [content]
  [:div
   [:h2.title (str (:artist content) " - " (:name content))]
   [song/listing (:song content)]])

(defn artist-detail [content]
  [:div
   [:h2.title (:name content)]
   [album/listing (:album content)]])

(defn most-recent [content]
  [:div
   [:h2.title "Recently played"]
   [album/listing (:album content)]])

(defn sidebar [user]
  [:aside.menu.section
   [:p.menu-label "Music"]
   [:ul.menu-list
    [:li [:a "By artist"]]
    [:li [:a "Top rated"]]
    [:li [:a "Most played"]]]
   [:p.menu-label "Playlists"]
   [:p.menu-label "Shares"]
   [:p.menu-label "Podcasts"]
   [:p.menu-label "User area"]
   [:ul.menu-list
    [:li [:a "Settings"]]
    [:li [:a
          {:on-click #(dispatch [::events/logout]) :href "#"}
          (str "Logout (" (:name user) ")")]]]])

;; putting everything together

(defn app [route-id params query]
  (let [user @(subscribe [::subs/user])
        content @(subscribe [::subs/current-content])]
    [:div
     [:main.columns
      [:div.column.is-2.sidebar
       [sidebar user]]
      [:div.column
       [:section.section
        [breadcrumbs content]
        (case route-id
          ::routes/main [most-recent content]
          ::routes/artist-view [artist-detail content]
          ::routes/album-view [album-detail content])]]]
     [bottom-bar]]))

(defn main-panel []
  (let [notifications @(subscribe [::subs/notifications])
        is-booting? @(subscribe [::subs/is-booting?])
        [route-id params query] @(subscribe [::subs/current-route])]
    (println "route-id" route-id (case route-id
                                   ::routes/login "::routes/login"
                                   "something else"))
    [:div
     [notification-list notifications]
     (if is-booting?
       [:div.app-loading>div.loader]
       (case route-id
         ::routes/login [login-form]
         [app route-id params query]))]))
