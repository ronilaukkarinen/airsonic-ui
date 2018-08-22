(ns airsonic-ui.api.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [airsonic-ui.api.events :as events]
            [airsonic-ui.fixtures :as fixtures]))

(deftest api-failure-notifcations
  (testing "Should show an error notification when airsonic responds with an error"
    (let [fx (events/good-api-response {} [:_ (:error fixtures/responses)])
          ev (:dispatch fx)]
      (is (= :notification/show (first ev)))
      (is (= :error (second ev))))))
