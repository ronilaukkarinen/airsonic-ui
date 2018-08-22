(ns airsonic-ui.routes
  (:require [bide.core :as r]
            [cljs.reader :refer [read-string]]
            [re-frame.core :as re-frame]))

(def default-route ::login)

(def router
  (r/router [["/" ::login]
             ["/main" ::main]
             ["/artist/:id" ::artist-view]
             ["/album/:id" ::album-view]]))

;; use this in views to construct a url
(defn url-for
  ([k] (url-for k {}))
  ([k params] (str "#" (r/resolve router k params))))

;; which routes need valid login credentials?
(def protected-routes #{::main ::artist-view ::album-view})

;; which data should be requested for which route? can either be a vector or a function returning a vector

(defmulti -route-events
  "Returns the events that take care of correct data being fetched."
  (fn [route-id & _] route-id))

(defmethod -route-events :default [route-id params query] nil)

(defmethod -route-events ::main
  [route-id params query]
  [:api/request "getAlbumList2" {:type "recent"
                                 :size 18}])

(defmethod -route-events ::artist-view
  [route-id params query]
  [:api/request "getArtist" (select-keys params [:id])])

(defmethod -route-events ::album-view
  [route-id params query]
  [:api/request "getAlbum" (select-keys params [:id])])

;; shouldn't need to change anything below

(defn- n-events?
  "Predicate that tells us whether a vector is suitable for :dispatch-n"
  [ev-vec]
  (or (vector? (first ev-vec))))

(defn route-events
  "Returns a normalized list of event vectors for a given route."
  [route-id params query]
  (let [ev-vec (-route-events route-id params query)]
    (if (n-events? ev-vec) ev-vec [ev-vec])))

;; subscription returning the matched route for the current hashbang

(re-frame/reg-sub :routes/current-route (fn [db _] (:routes/current-route db)))

;; NOTE: There is some duplication here. The route events are provided as a
;; subscription but they are also invoked directly in events.cljs. It didn't
;; seem to justify pulling in a whole library and we need it in our top most view

(re-frame/reg-sub
 :routes/events-for-current-route
 (fn [db _] (re-frame/subscribe [:routes/current-route]))
 (fn [current-route _] (apply route-events current-route)))

;; these are helper effects we can use to navigate; the first two manage an atom
;; holding credentials, which is necessary to restrict certain routes, and the
;; last one is used for actual navigation

;; the event to initialize navigation is implemented so the coeffect map is
;; returned unaltered, we just need access to the current app database for
;; authentication, which we get with an interceptor

(defonce ^:private credentials (atom nil))

(def do-navigation
  "An interceptor which performs the navigation after looking up current
  credentials in the app database"
  (re-frame.core/->interceptor
   :id :routes/do-navigation
   :after (fn do-navigation [context]
            (let [[_ & [route]] (get-in context [:coeffects :event])
                  ;; because :routes/do-navigation is both an event handler and
                  ;; an interceptor, we know that when handling the event (see
                  ;; below) the credentials aren't altered anymore
                  credentials'(get-in context [:coeffects :db :credentials])]
              (println "calling do-navigation with" route credentials')
              (reset! credentials credentials')
              (apply r/navigate! router route)
              context))))

(re-frame/reg-event-fx :routes/do-navigation do-navigation (fn [& _] nil))

(defn can-access? [route]
  (or (not (protected-routes route))
      (:verified? @credentials)))

(defn on-navigate
  [route-id params query]
  (println "on-navigate is called" route-id params query credentials)
  (if (can-access? route-id)
    (re-frame/dispatch [:routes/did-navigate route-id params query])
    (re-frame/dispatch [:routes/unauthorized route-id params query])))

(defn encode-route
  "Takes a parsed route and returns a representation that's suitable for
  transportation in a uri component"
  [route]
  (js/encodeURIComponent (str route)))

(defn decode-route
  "Decodes and encoded route from a uri component into a parsed route"
  [encoded-route]
  (read-string (js/decodeURIComponent encoded-route)))

(defn current-route
  "Returns the parsed route for window.location.hash"
  []
  (r/match router (subs (.. js/window -location -hash) 1)))

;; add the current route to our coeffect map
(re-frame/reg-cofx
 :routes/current-route
 (fn [coeffects _]
   (assoc coeffects :routes/current-route (current-route))))

;; add route into from a URL parameter to our coeffect map
(re-frame/reg-cofx
 :routes/from-query-param
 (fn [coeffects param]
   ;; this allows us to encode a complete route in a url fragment; useful for
   ;; doing redirects
   (let [[_ _ query] (current-route)
         from-param (some-> (get query param) (decode-route))]
     (assoc-in coeffects [:routes/from-query-param param] from-param))))

(defn start-routing!
  "Initializes the router and makes sure the correct events get dispatched."
  ([] (r/start! router {:default default-route
                        :on-navigate on-navigate}))
  ([_] (start-routing!))) ;; <- 1-arity is for the re-frame effect exposed below

(re-frame/reg-fx
 :routes/start-routing start-routing!)
