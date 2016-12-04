(ns zanbai.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub]]
            [zanbai.db :as db]))

(defn logged-in? [db]
  (not (nil? (::db/username db))))

(defn login-pending? [db] (boolean (::db/login-pending? db)))

(defn username [db] (::db/username db))

(defn users [db] (keys (::db/users db)))

(defn selected-users [db]
  (let [users (::db/users db)]
    (filter #(get-in users [% ::db/selected?]) (keys users))))

(defn conversations [db] (keys (::db/conversations db)))

(defn messages [db [uuid]] (get-in db [::db/conversations uuid ::db/messages]))

(defn participants [db [_ uuid]] (get-in db [::db/conversations uuid ::db/participants]))

;; TODO: can we do this in a loop?
(reg-sub :logged-in? logged-in?)
(reg-sub :login-pending? login-pending?)
(reg-sub :username username)
(reg-sub :users users)
(reg-sub :selected-users selected-users)
(reg-sub :conversations conversations)
(reg-sub :messages messages)
(reg-sub :participants participants)
