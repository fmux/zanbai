(ns zanbai.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [zanbai.core :refer [app-state]]))

;TODO: put this and the next somewhere else!
(defn send-message [from conversation text]
  (loop []
    (let
      [
        current-state @app-state
        conversations (current-state :conversations)
        participants (filter #(contains? (conversations %) conversation) (keys conversations))
        other-participants (filter #(not= from %) participants)
        new-state (reduce (fn [state participant] update-in state [:conversations participant conversation] #(conj % {:from from :text text})) current-state other-participants)
      ]
      ; TODO: this one we can do with swap!
      (if (compare-and-set! app-state current-state new-state)
        true
        (recur)
      )
    )
  )
)

(defn get-pending-messages [user]
  (loop []
    (let
      [
        current-state @app-state
        map-path [:conversations user]
        messages-for-user (get-in current-state map-path)
        new-state (reduce #(assoc-in %1 [map-path %2] ()) current-state (keys messages-for-user))
      ]
      (if (compare-and-set! app-state current-state new-state)
        messages-for-user
        (recur)
      )
    )
  )
)

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (POST "/login" request
    (let [username (get-in request [:body "username"])]
      (if (some #{username} (:users @app-state))
        {
          :status 409  ; Conflict
          :headers {"Content-Type" "application/json"}
          :body { :error-message (str "User " username " already exists") }
        }
        (do
          (swap! app-state update :users conj username)
          {
            :status 200
            :headers {"Content-Type" "application/json"}
            :body { :users (:users @app-state) }
          }
        )
      )
    )
  )
  ; login
  ; logout
  ; create conversation
  ; add user to conversation
  ; leave conversation
  (POST "/send_message/:from/:conversation" [from conversation]
    ; TODO: from should be set from cookie!!!
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (send-message from conversation "abcdefgh")  ; TODO: how to get text from JSON body???
    }
  )
  (GET "/get_pending_messages/:user" [user]
    ; TODO: user should be set from cookie!!!
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body {:pending-messages (get-pending-messages user)
            :users (:users @app-state)}
    }
  )
  (resources "/")
)

(def handler (-> routes wrap-json-body wrap-json-response))

(def dev-handler (wrap-reload handler))
