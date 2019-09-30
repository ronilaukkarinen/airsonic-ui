(ns lastfm.client
  (:require
    [org.httpkit.client :as http]
    [clojure.data.json :as json]
    [clojure.walk :refer [stringify-keys]]))

(def ^:private base-url "http://ws.audioscrobbler.com/2.0/")

(defn ^:private md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
         (doto (java.security.MessageDigest/getInstance "MD5")
               (.reset)
               (.update (.getBytes token)))]
       (.toString
         (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
         16))) ; Use base16 i.e. hex

(defn signed-params [query-params api-secret]
  (let [params-wo-format (dissoc query-params :format) ;; lastfm api bug. it doesn't include format params in signature
        sorted-param-string (reduce str (flatten (seq (into (sorted-map) (stringify-keys params-wo-format)))))
        res-str (str sorted-param-string api-secret)
        signature (md5 res-str)]
    (assoc query-params :api_sig signature)))

(defn make-req [{:keys [method params api-secret]}]
  (let [query-params (assoc params :method method :format "json")
        p (promise)]
    (println (signed-params query-params api-secret))
    (http/get base-url {:query-params (signed-params query-params api-secret)}
              (fn [{:keys [body error]}]
                (if error
                  (deliver p {:error error})
                  (deliver p {:body (json/read-str body)}))))
    p))

(defn get-token [api-key api-secret]
    (let [res @(make-req {:method "auth.getToken" :params {:api_key api-key} :api-secret api-secret})]
      (get-in res [:body "token"])))

(defn get-session [api-key api-secret token]
  (let [res @(make-req {:method "auth.getSession" :params {:api_key api-key :token token} :api-secret api-secret})]
    res))

(defn get-auth-url [api-key token]
  (str "http://www.last.fm/api/auth/?api_key=" api-key "&token=" token))

(def api-key "ba9c13e15382404dfb046dbbecac3424")

(def api-secret "634c9c84599e82544b86ee2967312967")

(let [token (get-token api-key api-secret)]
  (get-auth-url api-key token))

;; Your application needs to open a web browser and
;; send the user to last.fm/api/auth with your API
;; key and auth token as parameters. Use an HTTP GET
;; request. Your request will look like this:

;; http://www.last.fm/api/auth/?api_key=xxxxxxxxxxx&token=xxxxxxxx
;; If the user is not logged in to Last.fm, they will be
;; redirected to the login page before being asked to grant
;; your application permission to use their account. On this
;; page they will see the name of your application, along with
;; the application description and logo as supplied in Section 1.
;; Once the user has granted your application permission to use
;; their account, the browser-based process is over and the user
;; is asked to close their browser and return to your application.

(get-session api-key api-secret token)
;; => {:body {"session" {"name" "name", "key" "8482ded6ddf53598391c02a0c2c57b6b", "subscriber" "0"}}}

(ns airsonic-ui.components.audio-player.events
  (:require [re-frame.core :as rf]
            [airsonic-ui.audio.playlist :as playlist]
            [airsonic-ui.api.helpers :as api]))

; sets up the db, starts to play a song and adds the rest to a playlist
(defn play-all-songs [{:keys [db]
                       :routes/keys [current-route]} [_ songs start-idx]]
  (let [playlist (-> (playlist/->playlist songs :playback-mode :linear :repeat-mode :repeat-all :source current-route)
                     (playlist/set-current-song start-idx))]
    {:audio/play (api/stream-url (:credentials db) (playlist/current-song playlist))
     :db (assoc-in db [:audio :current-playlist] playlist)}))

(rf/reg-event-fx
 :audio-player/play-all
 [(rf/inject-cofx :routes/current-route)]
 play-all-songs)

(rf/reg-event-db
 :audio-player/set-playback-mode
 (fn [db [_ playback-mode]]
   (update-in db [:audio :current-playlist] #(playlist/set-playback-mode % playback-mode))))

(rf/reg-event-db
 :audio-player/set-repeat-mode
 (fn [db [_ repeat-mode]]
   (update-in db [:audio :current-playlist] #(playlist/set-repeat-mode % repeat-mode))))

(rf/reg-event-fx
 :audio-player/next-song
 (fn [{:keys [db]} _]
   (let [db (update-in db [:audio :current-playlist] playlist/next-song)
         next (playlist/current-song (get-in db [:audio :current-playlist]))]
     {:db db
      :audio/play (api/stream-url (:credentials db) next)})))

(rf/reg-event-fx
 :audio-player/previous-song
 (fn [{:keys [db]} _]
   (let [db (update-in db [:audio :current-playlist] playlist/previous-song)
         song (playlist/current-song (get-in db [:audio :current-playlist]))]
     {:db db
      :audio/play (api/stream-url (:credentials db) song)})))

(defn set-current-song [{:keys [db]} [_ idx]]
  (let [db (update-in db [:audio :current-playlist] playlist/set-current-song idx)
        song (playlist/current-song (get-in db [:audio :current-playlist]))]
    {:db db
     :audio/play (api/stream-url (:credentials db) song)}))

(rf/reg-event-fx :audio-player/set-current-song set-current-song)

(rf/reg-event-fx
 :audio-player/enqueue-next
 [(rf/inject-cofx :routes/current-route)]
 (fn [{:keys [db]
       :routes/keys [current-route]} [_ song]]
   {:db (update-in db [:audio :current-playlist] #(playlist/enqueue-next % song current-route))}))

(rf/reg-event-fx
 :audio-player/enqueue-last
 [(rf/inject-cofx :routes/current-route)]
 (fn [{:keys [db]
       :routes/keys [current-route]} [_ song]]
   {:db (update-in db [:audio :current-playlist] #(playlist/enqueue-last % song current-route))}))

(rf/reg-event-db
 :audio-player/move-song
 (fn [db [_ from-idx to-idx]]
   (update-in db [:audio :current-playlist] #(playlist/move-song % from-idx to-idx))))

(rf/reg-event-fx
 :audio-player/toggle-play-pause
 (fn [_ _]
   {:audio/toggle-play-pause nil}))

(defn remove-song [{:keys [db]} [_ song-idx]]
  (let [song-removed (update-in db [:audio :current-playlist] #(playlist/remove-song % song-idx))]
    (cond-> {:db song-removed}
      (nil? (playlist/current-song (get-in song-removed [:audio :current-playlist])))
      (assoc :audio/stop nil))))

(rf/reg-event-fx :audio-player/remove-song remove-song)

(defn audio-update
  "Reacts to audio events fired by the HTML5 audio player and plays the next
  track if necessary."
  [{:keys [db]} [_ status]]
  (cond-> {:db (assoc-in db [:audio :playback-status] status)}
    (:ended? status) (assoc :dispatch [:audio-player/next-song])))

(rf/reg-event-fx :audio/update audio-update)

(rf/reg-event-fx
 :audio-player/seek
 (fn [{:keys [db]} [_ percentage]]
   (let [duration (:duration (playlist/current-song (get-in db [:audio :current-playlist])))]
     {:audio/seek [percentage duration]})))

(rf/reg-event-fx
 :audio-player/set-volume
 (fn [_ [_ percentage]]
   {:audio/set-volume percentage}))

(rf/reg-event-fx
 :audio-player/increase-volume
 (fn [_ _]
   {:audio/increase-volume nil}))

(rf/reg-event-fx
 :audio-player/decrease-volume
 (fn [_ _]
   {:audio/decrease-volume nil}))
