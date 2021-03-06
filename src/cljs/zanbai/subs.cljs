(ns zanbai.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :logged-in?
 (fn [db]
   (contains? db :username)))

(reg-sub
 :login-pending?
 (fn [db]
   (contains? db :login-pending?)))

(reg-sub
 :username
 (fn [db]
   (:username db)))

(reg-sub
 :users
 (fn [db]
   (:users db)))

(reg-sub
 :selected-users
 (fn [db]
   (:selected-users db)))

(reg-sub
 :conversations
 (fn [db]
   (:conversations db)))
