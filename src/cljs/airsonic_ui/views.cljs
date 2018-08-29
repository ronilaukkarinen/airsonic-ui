(ns airsonic-ui.views
  "This module contains the outmost layer of our app views. It makes sure that
  the proper subscriptions are run and arranges the complete layout."
  (:require [re-frame.core :refer [dispatch subscribe]]
            [airsonic-ui.routes :as routes :refer [url-for]]
            [airsonic-ui.events :as events]
            [airsonic-ui.subs :as subs]
            [airsonic-ui.helpers :refer [add-classes]]

            [airsonic-ui.views.notifications :refer [notification-list]]
            [airsonic-ui.views.breadcrumbs :refer [breadcrumbs]]
            [airsonic-ui.views.audio-player :refer [audio-player]]
            [airsonic-ui.views.login :refer [login-form]]
            [airsonic-ui.components.search.views :as search]
            [airsonic-ui.components.library.views :as library]
            [airsonic-ui.components.artist.views :as artist]
            [airsonic-ui.components.collection.views :as collection]))

(defn sidebar [user]
  [:aside.menu.section
   [search/form]
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

(defn app [route-id params query]
  (let [user @(subscribe [::subs/user])
        ;; TODO: Move this to a layer 3 subscription ↓
        route-events @(subscribe [:routes/events-for-current-route])
        content @(subscribe [:api/route-data route-events])]
    [:div
     [:main.columns
      [:div.column.is-2.sidebar
       [sidebar user]]
      [:div.column.is-10
       [:section.section
        [breadcrumbs content]
        (case route-id
          ::routes/library [library/main [route-id params query] content]
          ::routes/artist-view [artist/detail content]
          ::routes/album-view [collection/detail content]
          ::routes/search [search/results content])]]]
     [audio-player]]))

(defn main-panel []
  (let [notifications @(subscribe [::subs/notifications])
        is-booting? @(subscribe [::subs/is-booting?])
        [route-id params query] @(subscribe [:routes/current-route])]
    [(add-classes :div route-id)
     [notification-list notifications]
     (if is-booting?
       [:div.app-loading>div.loader]
       (case route-id
         ::routes/login [login-form]
         [app route-id params query]))]))
