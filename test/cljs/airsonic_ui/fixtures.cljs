(ns airsonic-ui.fixtures)

(def credentials {:u "username"
                  :p "cleartext-password"
                  :server "https://demo.airsonic.io"})

(def responses {:error {:subsonic-response
                        {:error {:code 50
                                 :message "Incompatible Airsonic REST protocol version. Server must upgrade."}
                         :status "failed"
                         :version "1.15.0"}}
                :ok {:subsonic-response
                     {:scanStatus {:count 10326
                                   :scanning false}
                      :status "ok"
                      :version "1.15.0"}}
                :ping-success {:subsonic-response {:status "ok"
                                                   :version "1.15.0"}}
                :auth-success {:subsonic-response
                               {:status "ok",
                                :version "1.15.0",
                                :user
                                {:videoConversionRole false,
                                 :playlistRole true,
                                 :shareRole true,
                                 :podcastRole true,
                                 :email "admin@example.com",
                                 :streamRole true,
                                 :folder [0],
                                 :username "admin",
                                 :scrobblingEnabled false,
                                 :adminRole true,
                                 :settingsRole true,
                                 :commentRole true,
                                 :jukeboxRole true,
                                 :coverArtRole true,
                                 :downloadRole true,
                                 :maxBitRate 320,
                                 :uploadRole true}}}
                :auth-failure {:subsonic-response {:status "failed"
                                                   :version "1.15.0"
                                                   :error {:code 40
                                                           :message "Wrong username or password."}}}})

(def artist
  {:id "499", :name "Tomemitsu", :coverArt "ar-497", :albumCount 1})

(def album
  {:artistId "258",
   :name "Tocotronic",
   :songCount 26,
   :created "2017-12-31T08:18:45.000Z",
   :duration 7383,
   :artist "Tocotronic",
   :year 2015,
   :id "439",
   :coverArt "al-439"})

(def song
  {:artistId 42,
   :path "DJ Koze/DJ Koze - Reincarnations Part 2, The Remix Chapter 2009-2014/14. Apparat - Black Water (DJ Koze Remix).mp3",
   :suffix "mp3",
   :isDir false,
   :bitRate 320,
   :parent 3556,
   :albumId 382,
   :type "music",
   :created "2017-06-28T19:07:02.000Z",
   :duration 317,
   :artist "Apparat",
   :isVideo false,
   :size 12850290,
   :title "Black Water (DJ Koze Remix)",
   :playCount 0
   :year 2014,
   :id 3562,
   :coverArt 3556,
   :contentType "audio/mpeg",
   :album "Reincarnations, Pt. 2 - The Remix Chapter 2009 - 2014",
   :track 14})

(def playback-status
  {:ended? false
   :loop? false
   :muted? false
   :paused? false
   :current-src "https://londe.arnes.space/rest/stream?f=json&c=airsonic-ui-cljs&v=1.15.0&id=9574&u=arne&p=27h-%25bO%5B8-.ys%40SQ%7Bg%24-%5B5NZkX%7Dw%24NNwY%263DPATi%2CgaFoH%40e"
   :current-time 3.477029})

(def podcast-episode
  {:genre "Vocal",
   :description
   "Themen der Sendung: Druck auf Maaßen nach Äußerungen zu Chemnitz wächst, Köthen: 22-Jähriger stirbt nach Streit an Herzversagen, Parlamentswahl in Schweden, Russland und Syrien setzen Luftangriffe auf syrische Provinz Idlib fort, Tote und Verletzte bei Ausschreitungen im irakischem Basra, Nordkorea feiert 70. Jubiläum seiner Staatsgründung, Zahl der Toten nach Erdbeben in Japan steigt auf 39, Pläne von CDU und CSU: Fluggesellschaften sollen Auskunft über Verspätungen  geben, Menschenkette in Dangast als Zeichen gegen Flüchtlingssterben im Mittelmeer, Das Wetter",
   :suffix "mp3",
   :isDir false,
   :bitRate 64,
   :parent "10409",
   :channelId "4",
   :type "podcast",
   :created "2018-09-09T19:41:13.000Z",
   :duration 965,
   :artist "Tagesschau (Audio-Podcast)",
   :isVideo false,
   :publishDate "2018-09-09T18:00:00.000Z",
   :size 7812758,
   :title "09.09.2018 - tagesschau 20:00 Uhr",
   :playCount 0,
   :year 2018,
   :streamId "11181",
   :status "completed",
   :id "507",
   :coverArt "10409",
   :contentType "audio/mpeg",
   :album "tagesschau",
   :track 1})
