(ns zanbai.events
  (:require [re-frame.core
             :refer [reg-event-db reg-event-fx ->interceptor trim-v debug
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
                  spec ::db/db]
              (if (s/valid? spec new-db)
                context
                (do
                  ;; ideally the error message should just say "spec check failed"
                  ;; with the rest of the info stuck in the map. However, at the
                  ;; moment fighweel does not display the info map. See
                  ;; https://github.com/bhauman/lein-figwheel/issues/487
                  (throw (ex-info
                          (str "spec check failed: " (s/explain-str spec new-db))
                          {:old-db old-db
                           :new-db new-db
                           :explain-data (s/explain-data spec new-db)}))
                  (assoc-effect context :db old-db)))))))

(def standard-interceptors [debug trim-v check-spec])


;; handler functions
(defn initialize-db [_ _] db/default-db)

(defn send-login-request [{:keys [db]} [username]]
  {:db (assoc db ::db/login-pending? true)
   :http-xhrio {:method          :post
                :uri             "/login"
                :params          {:username username}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:login-succeeded username]
                :on-failure      [:login-failed]}})

(defn login-succeeded [{:keys [db]} [username result]]
  {:db (-> db
           (dissoc ::db/login-pending?)
           (assoc ::db/username username ::db/users (into {} (map #(vector % {}) (:users result)))))
   :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]})

(defn login-failed [db [result]]
  (-> db
      (dissoc ::db/login-pending?)
      (update ::db/error-messages conj (get-in result [:response :error-message]))))

(defn logout [{:keys [db]} _]
  {:http-xhrio {:method          :post
                :uri             "/logout"
                :params          (select-keys db [::db/username])
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:logout-succeeded]
                :on-failure      [:logout-failed]}})

(defn logout-succeeded [db _]
  (dissoc db ::db/username))

(defn logout-failed [db [result]]
  (update db ::db/error-messages conj (get-in result [:response :error-message])))

(defn get-pending-messages [{:keys [db]} _]
  {:http-xhrio {:method          :get
                :uri             (str "/get_pending_messages/" (::db/username db))
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:got-pending-messages]
                :on-failure      [:getting-pending-messages-failed]}})

(defn got-pending-messages [{:keys [db]} [result]]
  (let [conversations (keys (:pending-messages result))
        messages->db (fn [old-db conversation]
                       (let [new-messages (get-in result [:pending-messages conversation])]
                         (update-in old-db [::db/conversations conversation]
                                    #(into (or % []) new-messages))))
        new-db (-> (reduce messages->db db conversations)
                   (assoc ::db/users (:users result)))]
    {:db new-db
     :dispatch-later [{:ms 1000 :dispatch [:get-pending-messages]}]}))

(defn getting-pending-messages-failed [db [result]]
  (update db ::db/error-messages conj (get-in result [:response :error-message])))

(defn toggle-user [db [user]]
  (update-in db [::db/users user ::db/selected?] not))

(defn start-conversation [{:keys [db]} [users]]
  {:http-xhrio
   {:method          :post
    :uri             (str "/start_conversation/" (::db/username db))
    :params          {:users users}
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})
    :on-success      [:started-conversation]
    :on-failure      [:starting-conversation-failed]}
   :db (assoc db ::db/selected-users #{})})

(defn started-conversation [db [result]]
  (let [conversation (keyword (:uuid result))
        participants (:users result)]
    (assoc-in db [::db/conversations conversation]
              {::db/participants participants
               ::db/messages []})))

(defn starting-conversation-failed [db [result]]
  (update db ::db/error-messages conj (get-in result [:response :error-message])))

(defn send-message [{:keys [db]} [uuid text]]
  {:http-xhrio
   {:method          :post
    :uri             (str "/send_message/" (::db/username db) "/" uuid)
    :params          {:text text}
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})
    :on-failure      [:sending-message-failed]}})

(defn sending-message-failed [db [result]]
  (update db ::db/error-messages conj (get-in result [:response :error-message])))


;; register event handlers
(defn std-reg-event-db
  ([id handler-fn]
   (std-reg-event-db id nil handler-fn))
  ([id interceptors handler-fn]
   (reg-event-db id [interceptors standard-interceptors] handler-fn)))

(defn std-reg-event-fx
  ([id handler-fn]
   (std-reg-event-fx id nil handler-fn))
  ([id interceptors handler-fn]
   (reg-event-fx id [interceptors standard-interceptors] handler-fn)))

(std-reg-event-db :initialize-db initialize-db)
(std-reg-event-fx :send-login-request send-login-request)
(std-reg-event-fx :login-succeeded login-succeeded)
(std-reg-event-db :login-failed login-failed)
(std-reg-event-fx :logout logout)
(std-reg-event-db :logout-succeeded logout-succeeded)
(std-reg-event-db :logout-failed logout-failed)
(std-reg-event-fx :get-pending-messages get-pending-messages)
(std-reg-event-fx :got-pending-messages got-pending-messages)
(std-reg-event-db :getting-pending-messages-failed getting-pending-messages-failed)
(std-reg-event-db :toggle-user toggle-user)
(std-reg-event-fx :start-conversation start-conversation)
(std-reg-event-db :started-conversation started-conversation)
(std-reg-event-db :starting-conversation-failed starting-conversation-failed)
(std-reg-event-fx :send-message send-message)
(std-reg-event-db :sending-message-failed sending-message-failed)
