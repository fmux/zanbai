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
              [:h3 "Welcome to zanbai!"
                ;(when @login-pending? [:span " " [:small "Logging in..."]])  ;TODO: make less intrusive
              ]
              [:div.input-group
                [:input#username.form-control
                  {
                    :type "text"
                    :placeholder "Enter Username"
                    :required true
                  }
                ]
                [:span.input-group-btn
                  [:button#login-button.btn.btn-primary
                    {
                      :type "submit"
                      :disabled @login-pending?  ;TODO: disable also when input is empty
                      :on-click #(dispatch [:send-login-request (.val (js/$ "#username"))])
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
                [:div.pull-right.btn-group
                  [:span#my-user.dropdown-toggle {:data-toggle "dropdown"}
                    @username " "
                    [:span.caret]
                  ]
                  [:ul.dropdown-menu
                    ;TODO: prevent browser from navigating to "#"
                    [:li [:a {:href "#" :on-click #(do (dispatch [:logout]) false)} "Logout"]]
                  ]
                ]
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
