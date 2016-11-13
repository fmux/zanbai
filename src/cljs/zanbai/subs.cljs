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
