(ns ^:figwheel-load zanbai.subs-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [zanbai.subs :refer [logged-in? login-pending? username
                                 users selected-users conversations]]
            [zanbai.db :as db]
            [devtools.defaults :as d]))

;; TODO: it seems we are duplicating information here in the assocs.
;; Should we create an extra layer of redirection for putting info
;; into the app DB (like subscriptions, only in the other direction)?
(deftest test-logged-in?
  (testing "Subscription :logged-in?"
    (is (false? (logged-in? db/default-db)))
    (is (true? (logged-in? (assoc db/default-db ::db/username ""))))
    (is (true? (logged-in? (assoc db/default-db ::db/username "Me"))))))

(deftest test-login-pending?
  (testing "Subscription :login-pending?"
    (is (false? (login-pending? db/default-db)))
    (is (false? (login-pending? (assoc db/default-db ::db/login-pending? false))))
    (is (true? (login-pending? (assoc db/default-db ::db/login-pending? true))))
    (is (true? (login-pending? (assoc db/default-db ::db/login-pending? "bananas"))))))

(deftest test-username
  (testing "Subscription :username"
    (is (nil? (username db/default-db)))
    (is (= "Me" (username (assoc db/default-db ::db/username "Me"))))))

(deftest test-users
  (testing "Subscription :users"
    (is (empty? (users db/default-db)))
    (is (= '("Me") (users (assoc db/default-db ::db/users {"Me" {}}))))))

(deftest test-selected-users
  (let [with-users #(assoc db/default-db ::db/users %)]
    (testing "Subscription :selected-users"
      (testing "No one selected"
        (is (empty? (selected-users db/default-db)))
        (is (empty? (selected-users (with-users {"Me" {}}))))
        (is (empty? (selected-users (with-users {"Me" {::db/selected? nil}}))))
        (is (empty? (selected-users (with-users {"Me" {::db/selected? false}})))))
      (testing "One user selected"
        (is (= '("Me") (selected-users (with-users {"Me" {::db/selected? true}}))))
        (is (= '("Me") (selected-users (with-users {"Me" {::db/selected? "bananas"}}))))
        (is (= '("Me") (selected-users (with-users {"Me" {::db/selected? true}
                                                    "You" {}}))))
        (is (= '("Me") (selected-users (with-users {"Me" {::db/selected? true}
                                                    "You" {::db/selected? false}})))))
      (testing "Multiple users selected"
        (is (= #{"Me" "You"} (set (selected-users (with-users {"Me" {::db/selected? true}
                                                               "You" {::db/selected? true}})))))))))

(deftest test-conversations
  (testing "Subscription :conversations"
    (is (empty? (conversations db/default-db)))))  ; TODO: some more tests here
