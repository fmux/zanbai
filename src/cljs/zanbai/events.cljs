(ns zanbai.events
    (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
              [zanbai.db :as db]
              [day8.re-frame.http-fx]
              [ajax.core :as ajax]
              ))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-fx
  :send-login-request
  (fn [{db :db} [_ username]]
    {:db (assoc db :login-pending? true)
     :http-xhrio {:method          :post
                  :uri             "/login"
                  :params          {:username username}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:login-succeeded username]
                  :on-failure      [:login-failed]}}))

(reg-event-db
  :login-succeeded
  (fn [db [_ username result]]
    assoc (dissoc db :login-pending?) :username username :users (:users result)))

(reg-event-db
  :login-failed
  (fn [db [_ result]]
    update (dissoc db :login-pending?) :error-messages conj (get-in result [:response :error-message])))
