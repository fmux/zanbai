(ns zanbai.events-test
  (:require [zanbai.events :as events]
            [clojure.test :as t]))

(t/deftest test-got-pending-messages
  (let [db {:username "Fabi"
            :users ["Adam" "Fabi"]
            :conversations {}}
        result {:users ["Adam" "Fabi"]
                :pending-messages {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Hello!"}]}}
        expected {:username "Fabi"
                  :users ["Adam" "Fabi"]
                  :conversations {"f193e160-b86f-11e6-a98b-4ede1694ae03" [{:from "Adam" :text "Hello!"}]}}]
    (t/is (events/got-pending-messages [{:db db} [nil result]]) expected)))
