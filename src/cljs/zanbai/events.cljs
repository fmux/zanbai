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
  (fn [{:keys [db]} [_ username]]
    {:db (assoc db :login-pending? true)
     :http-xhrio {:method          :post
                  :uri             "/login"
                  :params          {:username username}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:login-succeeded username]
                  :on-failure      [:login-failed]}}))

(reg-event-fx
  :login-succeeded
  (fn [{:keys [db]} [_ username result]]
    {:db (assoc (dissoc db :login-pending?) :username username :users (:users result))
     :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]}))

(reg-event-db
  :login-failed
  (fn [db [_ result]]
    (update (dissoc db :login-pending?) :error-messages conj (get-in result [:response :error-message]))))

(reg-event-fx
  :logout
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/logout"
                  :params          (select-keys db [:username])
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:logout-succeeded]
                  :on-failure      [:logout-failed]}}))

(reg-event-db
  :logout-succeeded
  (fn [db _]
    (dissoc db :username)))

(reg-event-db
  :logout-failed
  (fn [db [_ result]]
    (update db :error-messages conj (get-in result [:response :error-message]))))

(reg-event-fx
  :get-pending-messages
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             (str "/get_pending_messages/" (:username db))
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:got-pending-messages]
                  :on-failure      [:getting-pending-messages-failed]}}))

(reg-event-fx
  :got-pending-messages
  (fn [{:keys [db]} [_ result]]
    {:db (assoc db :users (:users result))
     :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]}))

(reg-event-db
  :getting-pending-messages-failed
  (fn [db [_ result]]
    (update db :error-messages conj (get-in result [:response :error-message]))))

(reg-event-db
 :toggle-user
 (fn [db [_ user]]
   (if (some #(= % user) (:selected-users db))
     (do (println "Removing user" user) (update-in db [:selected-users] disj user))
     (do (println "Adding user" user) (update-in db [:selected-users] conj user)))))

(reg-event-fx
 :start-conversation
 (fn [{:keys [db]} [_ users]]
   (do
     (println users)
     {:db (assoc db :selected-users #{})})))
