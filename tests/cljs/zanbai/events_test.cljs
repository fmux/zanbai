(ns ^:figwheel-load zanbai.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [zanbai.events :refer [got-pending-messages]]))

(deftest test-got-pending-messages
  (testing "Event :got-pending-messages"
    (let [db-initial {:username "Fabi"
                      :users ["Adam" "Fabi"]
                      :conversations {}}
          first-result {:users ["Adam" "Fabi"]
                        :pending-messages {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Hello!"}]}}
          first-db {:username "Fabi"
                    :users ["Adam" "Fabi"]
                    :conversations {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Hello!"}]}}
          first-expected {:db first-db
                          :dispatch-later [{:ms 1000, :dispatch [:get-pending-messages]}]}
          second-result {:users ["Adam" "Fabi"]
                         :pending-messages {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Anybody there?"}]}}
          second-db {:username "Fabi"
                     :users ["Adam" "Fabi"]
                     :conversations {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Hello!"}
                                                                             {:from "Adam" :text "Anybody there?"}]}}
          second-expected {:db second-db
                           :dispatch-later [{:ms 1000, :dispatch [:get-pending-messages]}]}]
      (testing "Starting new conversation"
        (is (= (got-pending-messages {:db db-initial} [nil first-result]) first-expected)))
      (testing "Adding new message to exisiting conversation"
        (is (= (got-pending-messages {:db first-db} [nil second-result]) second-expected))))))

(deftest supposed-to-fail
  (testing "This is supposed to fail!"
    (is (= 1 2))))
