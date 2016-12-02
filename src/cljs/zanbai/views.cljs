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
         [:form
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
              :on-click #(do (dispatch [:send-login-request (-> js/document (.getElementById "username") .-value)]) (.preventDefault %))
              }
             "Login"]]]]]]]]]))

(defn user-list []
  (let [username (subscribe [:username])
        users (subscribe [:users])
        other-users (reaction (filter #(not= % @username) @users))
        other-users-sorted (reaction (sort @other-users))
        selected-users (subscribe [:selected-users])]
    [:div#user-list.col-xs-12.col-sm-6.col-sm-push-6.col-md-4.col-md-push-8
     [:div.panel.panel-primary
      [:div.panel-heading
       [:h1.panel-title "Currently Online"
        [:div.pull-right.btn-group
         [:span#my-user.dropdown-toggle {:data-toggle "dropdown"}
          [:span.glyphicon.glyphicon-user]
          " " @username " "
          [:span.caret]]
         [:ul.dropdown-menu.dropdown-menu-right
          [:li [:a {:href "#" :on-click #(do (dispatch [:logout]) (.preventDefault %))} [:span.glyphicon.glyphicon-off] " Logout"]]]]]]
      [:div.list-group
       (doall (for [user @other-users-sorted]
                [(if (some #(= % user) @selected-users)
                   :button.other-user.list-group-item.active
                   :button.other-user.list-group-item)
                 {:key user
                  :on-click #(do (dispatch [:toggle-user user]) (.preventDefault %))}
                 user]))
       (if (empty? @other-users)
         [:button.start-conversation.list-group-item
          "No users are currently online."]
         (if (empty? @selected-users)
           [:button.start-conversation.list-group-item
            "Select some users to start a conversation."]
           ; TODO: make active state stand out more (bootstrap-theme does a bad job)
           [:button.start-conversation.list-group-item.active
            {:on-click #(dispatch [:start-conversation @selected-users])}
            "Start conversation!"]))]]]))

(defn conversation-widget [conversation]
  (let [username (subscribe [:username])
        uuid (reaction (:uuid conversation))
        input-id (str @uuid "-message")
        users (reaction (:users conversation))
        other-users (reaction (filter #(not= % @username) @users))
        other-users-sorted (reaction (sort @other-users))]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h1.panel-title
       (let [first-users (vec (drop-last 2 @other-users-sorted))
             last-users (vec (take-last 2 @other-users-sorted))]
         (str
          "Conversation with "
          (apply str (flatten (interpose ", " (conj first-users (interpose " and " last-users)))))))]]
     [:div.messages.panel-body
      [:div.message.message-outbound "Hello! how are you?"]
      [:div.message.message-inbound "I'm fine! How are you?"]
      [:div.message.message-inbound "I'm also fine! Thanks for asking!"]]
     [:div.panel-footer
      [:form
       [:div.input-group
        [:input.form-control
         {:id input-id
          :type "text"
          :placeholder "Enter Message"
          :required true}
         ]
        [:span.input-group-btn
         [:button.btn.btn-primary
          {:type "submit"
           ;:disabled @login-pending?  ;TODO: disable also when input is empty
           :on-click #(do (dispatch [:send-message @uuid (-> js/document (.getElementById input-id) .-value)]) (.preventDefault %))}
          "Send"]]]]]]))

(defn main []
  (let [conversations (subscribe [:conversations])]
    [:div#main.container-fluid
     [:div.row
      [user-list]
      (for [conversation @conversations]
        [:div.col-xs-12.col-sm-6.col-sm-pull-6.col-md-4.col-md-pull-4
         {:key (:uuid conversation)}
         [conversation-widget conversation]])]]))

(defn app []
  (let [logged-in? (subscribe [:logged-in?])]
    (fn []
      [:div (if @logged-in? [main] [login])])))
