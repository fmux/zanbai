(ns zanbai.views
    (:require-macros [reagent.ratom :refer [reaction]])
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
                      :type "submit"
                      :disabled @login-pending?
                      :on-click #(dispatch [:send-login-request (-> js/document (.getElementById "username") .-value)])
                    }
                    "Login"]]]]]]]]))

(defn main []
  (let
    [
      username (subscribe [:username])
      users (subscribe [:users])
      other-users (reaction (filter #(not= % @username) @users))
      other-users-sorted (reaction (sort @other-users))
    ]
    [:div#main.container-fluid
      [:div.row
        [:div#user-list.col-xs-12.col-sm-6.col-sm-offset-6.col-md-4.col-md-offset-8
          [:div.panel.panel-primary
            [:div.panel-heading
              [:h1.panel-title "Currently Online"
                [:span.my-user.pull-right @username]
              ]
            ]
            [:div.list-group
              (for [user @other-users-sorted]
                [:button.other-user.list-group-item {:key user} user])]]]]]))

(defn app []
  (let [logged-in? (subscribe [:logged-in?])]
    (do
      (when config/debug? (println "rendering app"))
      (fn []
        [:div (if @logged-in? [main] [login])]))))
