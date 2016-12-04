(ns zanbai.events
  (:require [re-frame.core
             :refer [reg-event-db reg-event-fx ->interceptor trim-v
                     get-coeffect get-effect assoc-effect]]
            [zanbai.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [clojure.spec :as s]))

;; interceptors
(def check-spec
  (->interceptor
   :id    :check-spec
   :after (fn [context]
            (let [old-db (get-coeffect context :db)
                  new-db (get-effect context :db)
                  spec :db/db]
              (when-not (s/valid? spec new-db)
                (throw (ex-info (str "spec check failed: " (s/explain-str spec new-db)) {}))
                (assoc-effect context :db old-db))))))

(def default-interceptors [trim-v check-spec])


;; handler functions
(defn initialize-db [_ _] db/default-db)

(defn send-login-request [{:keys [db]} [username]]
  {:db (assoc db :login-pending? true)
   :http-xhrio {:method          :post
                :uri             "/login"
                :params          {:username username}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:login-succeeded username]
                :on-failure      [:login-failed]}})

(defn login-succeeded [{:keys [db]} [username result]]
  {:db (assoc (dissoc db :login-pending?) :username username :users (:users result))
   :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]})

(defn login-failed [db [result]]
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

(defn logout-failed [db [result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn get-pending-messages [{:keys [db]} _]
  {:http-xhrio {:method          :get
                :uri             (str "/get_pending_messages/" (:username db))
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:got-pending-messages]
                :on-failure      [:getting-pending-messages-failed]}})

(defn got-pending-messages [{:keys [db]} [result]]
  (let [conversations (keys (:pending-messages result))
        messages->db (fn [old-db conversation]
                       (let [new-messages (get-in result [:pending-messages conversation])]
                         (update-in old-db [:conversations conversation]
                                    #(into (or % []) new-messages))))
        new-db (-> (reduce messages->db db conversations)
                   (assoc :users (:users result)))]
    {:db new-db
     :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]}))

(defn getting-pending-messages-failed [db [result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn toggle-user [db [user]]
  (update-in db [:selected-users]
             (if (some #(= % user) (:selected-users db)) disj conj)
             user))

(defn start-conversation [{:keys [db]} [users]]
  {:http-xhrio
   {:method          :post
    :uri             (str "/start_conversation/" (:username db))
    :params          {:users users}
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})
    :on-success      [:started-conversation]
    :on-failure      [:starting-conversation-failed]}
   :db (assoc db :selected-users #{})})

(defn started-conversation [db [result]]
  (update db :conversations conj result))

(defn starting-conversation-failed [db [result]]
  (update db :error-messages conj (get-in result [:response :error-message])))

(defn send-message [{:keys [db]} [uuid text]]
  {:http-xhrio
   {:method          :post
    :uri             (str "/send_message/" (:username db) "/" uuid)
    :params          {:text text}
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})
    :on-failure      [:sending-message-failed]}})

(defn sending-message-failed [db [result]]
  (update db :error-messages conj (get-in result [:response :error-message])))


;; register event handlers
(reg-event-db :initialize-db default-interceptors initialize-db)
(reg-event-fx :send-login-request default-interceptors send-login-request)
(reg-event-fx :login-succeeded default-interceptors login-succeeded)
(reg-event-db :login-failed default-interceptors login-failed)
(reg-event-fx :logout default-interceptors logout)
(reg-event-db :logout-succeeded default-interceptors logout-succeeded)
(reg-event-db :logout-failed default-interceptors logout-failed)
(reg-event-fx :get-pending-messages default-interceptors get-pending-messages)
(reg-event-fx :got-pending-messages default-interceptors got-pending-messages)
(reg-event-db :getting-pending-messages-failed default-interceptors getting-pending-messages-failed)
(reg-event-db :toggle-user default-interceptors toggle-user)
(reg-event-fx :start-conversation default-interceptors start-conversation)
(reg-event-db :started-conversation default-interceptors started-conversation)
(reg-event-db :starting-conversation-failed default-interceptors starting-conversation-failed)
(reg-event-fx :send-message default-interceptors send-message)
(reg-event-db :sending-message-failed default-interceptors sending-message-failed)
