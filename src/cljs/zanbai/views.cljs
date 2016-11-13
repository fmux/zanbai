(ns zanbai.views
    (:require [re-frame.core :refer [subscribe dispatch]]
              [zanbai.config :as config]
              [clojure.pprint :refer [pprint]]))

(defn error-messages []
  (let [error-messages (subscribe [:error-messages])]
  [:span (with-out-str (pprint @error-messages))])
)

(defn login []
  (let [login-pending? (subscribe [:login-pending?])]
    [:div#login-form.jumbotron
      [:div.container
        [:div.row
          [:div.col-xs-12.col-sm-6.col-sm-offset-3.col-md-4.col-md-offset-4
            [:div#login-jumbotron.jumbotron
            [:h3 "Welcome to zanbai!"]
            [:div.input-group
              [:input#username.form-control
                {
                  :type "text"
                  :placeholder "Enter Username"
                }
              ]
              [:span.input-group-btn
                [:button#login-button.btn.btn-primary
                  {
                    :type "button"
                    :disabled @login-pending?
                    :on-click #(dispatch [:send-login-request (-> js/document (.getElementById "username") .-value)])
                  }
                  "Login"
                ]
              ]
            ]]
          ]
        ]
      ]
    ]
  )
)

(defn main []
  (let [username (subscribe [:username])]
    [:div
      [:h1 (str "Hello " @username)]
      ;TODO: make this work...
      ;[:div#users (-> (:users @app-state) (filter (not= (:username @app-state))) (map #([:span %])))]
    ]
  )
)

(defn app []
  (let [logged-in? (subscribe [:logged-in?])]
    (do
      (when config/debug? (println "rendering app"))
      (fn []
        [:div (if @logged-in? [main] [login])]))))
