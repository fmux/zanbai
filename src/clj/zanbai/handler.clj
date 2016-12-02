(ns zanbai.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clj-uuid :as uuid]
            [zanbai.core :refer [app-state send-message get-pending-messages]]))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (POST "/login" request
        (let [username (get-in request [:body "username"])]
          (if (some #{username} (:users @app-state))
            {:status 409  ; Conflict
             :headers {"Content-Type" "application/json"}
             :body { :error-message (str "User " username " already exists") }}
            (do
              (swap! app-state update :users conj username)
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body { :users (:users @app-state) }}))))
  (POST "/logout" request
        (let [username (get-in request [:body "username"])]
          (if (some #{username} (:users @app-state))
            (do
              (swap! app-state update :users #(remove #{username} %))
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body { :app-state @app-state }})
            {:status 409  ; Conflict  ;TODO: appropriate HTTP code?
             :headers {"Content-Type" "application/json"}
             :body { :error-message (str "User " username " not in user list!") }})))
  (POST "/start_conversation/:from" [from :as request]
        ; TODO: from should be set from cookie!!!
        (let [users (conj (get-in request [:body "users"]) from) 
              new-uuid (uuid/to-string (uuid/v1))]
          (do
            (doall
             (for [user users]
               ; TODO: maybe there is a more idiomatic way than doall for?
               (swap! app-state update-in [:conversations user] assoc new-uuid [])))
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body {:uuid new-uuid
                    :users users}})))
  ; add user to conversation
  ; leave conversation
  (POST "/send_message/:from/:conversation" [from conversation :as request]
        (do (send-message from conversation (get-in request [:body "text"]))
            ; TODO: from should be set from cookie!!!
            {:status 200
             :headers {"Content-Type" "application/json"}}))
  (GET "/get_pending_messages/:user" [user]
       ; TODO: user should be set from cookie!!! or body param
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body {:pending-messages (get-pending-messages user)
               :users (:users @app-state)}})
  (resources "/"))

(def handler (-> routes wrap-json-body wrap-json-response))

(def dev-handler (wrap-reload handler))
