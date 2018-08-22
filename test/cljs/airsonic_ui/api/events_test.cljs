(ns airsonic-ui.api.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [airsonic-ui.api.events :as events]
            [airsonic-ui.api.subs :as sub]
            [airsonic-ui.fixtures :as fixtures]))

(enable-console-print!)

(deftest api-failure-notifcations
  (testing "Should show an error notification when airsonic responds with an error"
    (let [fx (events/good-api-response {} [:_ (:error fixtures/responses)])
          ev (:dispatch fx)]
      (is (= :notification/show (first ev)))
      (is (= :error (second ev))))))

(deftest cached-api-requests
  (letfn [(cache [fx [endpoint params]]
            ;; this is just a thin helper function to avoid repetition
            (sub/response-for (:db fx) [:api/response-for endpoint params]))]
    (testing "Should be cached"
      (testing "when the response was successful"
        (let [endpoint "getScanStatus"
              successful (events/good-api-response {} [:api/good-response (:ok fixtures/responses) endpoint])
              unsuccessful (events/good-api-response {} [:api/good-response (:error fixtures/responses) endpoint])]
          (is (map? (cache successful [endpoint])))
          (is (nil? (cache unsuccessful [endpoint])))))
      (testing "in an unwrapped format"
        (let [endpoint "getScanStatus"
              fx (events/good-api-response {} [:api/good-response (:ok fixtures/responses) endpoint])]
          (is (= #{:count :scanning} (set (keys (cache fx [endpoint]))))))))
    (testing "When being issued"
      (let [endpoint "getScanStatus"
            fx (events/api-request {:db {:credentials (select-keys fixtures/credentials [:server])}}
                                   [:api/request endpoint])]
        (testing "should send an http request"
          (is (contains? fx :http-xhrio)))
        (testing "should indicate that a request is ongoing"
          (is (true? (:api/is-loading? (cache fx [endpoint]))) "for non-cached responses")
          (is (true? (-> (events/good-api-response fx [:api/good-response (:ok fixtures/responses) endpoint])
                         (events/api-request [:api/request endpoint])
                         (cache [endpoint])
                         :api/is-loading?)) "for cached responses"))
        (testing "should remove the indication that a request is ongoing when there is a response"
          (is (not (:api/is-loading? (-> (events/good-api-response fx [:api/good-response (:ok fixtures/responses) endpoint])
                                         (cache [endpoint])))) "for a good response")
          (is (not (:api/is-loading? (-> (merge fx (events/good-api-response fx [:api/good-response (:error fixtures/responses) endpoint]))
                                         (cache [endpoint])))) "when an error is returned")
          (is (not (:api/is-loading? (-> (merge fx (events/failed-api-response fx [:api/failed-response endpoint]))
                                         (cache [endpoint])))) "when communication with the server failed"))))
    (testing "Should be able to avoid the cache")))
