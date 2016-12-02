(ns zanbai.events
    (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
              [zanbai.db :as db]
              [day8.re-frame.http-fx]
              [ajax.core :as ajax]
              ))

(defn initialize-db [_ _] db/default-db)

(defn send-login-request [{:keys [db]} [_ username]]
  {:db (assoc db :login-pending? true)
   :http-xhrio {:method          :post
                :uri             "/login"
                :params          {:username username}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:login-succeeded username]
                :on-failure      [:login-failed]}})

(defn login-succeeded [{:keys [db]} [_ username result]]
  {:db (assoc (dissoc db :login-pending?) :username username :users (:users result))
   :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]})

(defn login-failed [db [_ result]]
  (update (dissoc db :login-pending?) :error-messages conj (get-in result [:response :error-message])))

(defn logout [{:keys [db]} _]
  {:http-xhrio {:method          :post
                :uri             "/logout"
                :params          (select-keys db [:username])
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:logout-succeeded]
                :on-failure      [:logout-failed]}})

(defn logout-succeeded [db _]
  (dissoc db :username))

(defn logout-failed [db [_ result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn get-pending-messages [{:keys [db]} _]
  {:http-xhrio {:method          :get
                :uri             (str "/get_pending_messages/" (:username db))
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:got-pending-messages]
                :on-failure      [:getting-pending-messages-failed]}})

(defn got-pending-messages [{:keys [db]} [_ result]]
  (let [new-db (reduce
                (fn [old-db conversation]
                  (update-in db [:conversations conversation]
                             (fn [messages]
                               (let [message (get-in result [:pending-messages conversation])]
                                 (if (nil? messages)
                                   [message]
                                   (conj messages message))))))
                db (keys (:pending-messages result)))]
    {:db (assoc new-db :users (:users result))
     :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]}))

(defn getting-pending-messages-failed [db [_ result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn toggle-user [db [_ user]]
  (update-in db [:selected-users]
             (if (some #(= % user) (:selected-users db)) disj conj)
             user))

(defn start-conversation [{:keys [db]} [_ users]]
  {:http-xhrio {:method          :post
                :uri             (str "/start_conversation/" (:username db))
                :params          {:users users}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:started-conversation]
                :on-failure      [:starting-conversation-failed]}
   :db (assoc db :selected-users #{})})

(defn started-conversation [db [_ result]]
  (update db :conversations conj result))

(defn starting-conversation-failed [db [_ result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn send-message [{:keys [db]} [_ uuid text]]
  {:http-xhrio {:method          :post
                :uri             (str "/send_message/" (:username db) "/" uuid)
                :params          {:text text}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-failure      [:sending-message-failed]}})

(defn sending-message-failed [db [_ result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(reg-event-db :initialize-db initialize-db)
(reg-event-fx :send-login-request send-login-request)
(reg-event-fx :login-succeeded login-succeeded)
(reg-event-db :login-failed login-failed)
(reg-event-fx :logout logout)
(reg-event-db :logout-succeeded logout-succeeded)
(reg-event-db :logout-failed logout-failed)
(reg-event-fx :get-pending-messages get-pending-messages)
(reg-event-fx :got-pending-messages got-pending-messages)
(reg-event-db :getting-pending-messages-failed getting-pending-messages-failed)
(reg-event-db :toggle-user toggle-user)
(reg-event-fx :start-conversation start-conversation)
(reg-event-db :started-conversation started-conversation)
(reg-event-db :starting-conversation-failed starting-conversation-failed)
(reg-event-fx :send-message send-message)
(reg-event-db :sending-message-failed sending-message-failed)
