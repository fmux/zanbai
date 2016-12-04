(ns ^:figwheel-load zanbai.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [zanbai.events :refer [got-pending-messages]]
            [zanbai.db :as db]))

(deftest test-got-pending-messages
  (testing "Event :got-pending-messages"
    (let [db-initial {::db/username "Fabi"
                      ::db/users ["Adam" "Fabi"]
                      ::db/conversations {}}
          first-result {:users ["Adam" "Fabi"]
                        :pending-messages {"f193e160-b86f-11e6-a98b-4ede1694ae03"
                                           [{:from "Adam" :text "Hello!"}]}}
          first-db {::db/username "Fabi"
                    ::db/users ["Adam" "Fabi"]
                    ::db/conversations {"f193e160-b86f-11e6-a98b-4ede1694ae03"
                                        [{:from "Adam" :text "Hello!"}]}}
          first-expected {:db first-db
                          :dispatch-later [{:ms 1000, :dispatch [:get-pending-messages]}]}
          second-result {:users ["Adam" "Fabi"]
                         :pending-messages {"f193e160-b86f-11e6-a98b-4ede1694ae03"
                                            [{:from "Adam" :text "Anybody there?"}]}}
          second-db {::db/username "Fabi"
                     ::db/users ["Adam" "Fabi"]
                     ::db/conversations {"f193e160-b86f-11e6-a98b-4ede1694ae03"
                                         [{:from "Adam" :text "Hello!"}
                                          {:from "Adam" :text "Anybody there?"}]}}
          second-expected {:db second-db
                           :dispatch-later [{:ms 1000, :dispatch [:get-pending-messages]}]}]
      (testing "Starting new conversation"
        (is (= (got-pending-messages {:db db-initial} [first-result]) first-expected)))
      (testing "Adding new message to exisiting conversation"
        (is (= (got-pending-messages {:db first-db} [second-result]) second-expected))))))
