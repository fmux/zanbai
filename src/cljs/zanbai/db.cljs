(ns zanbai.db
  (:require [cljs.spec :as s]
            [cljs-uuid-utils.core :as uuid]))

;; spec for re-frame.db/app-db

;; data types
(s/def ::user-name string?)
(s/def ::user-selected boolean?)  ; whether the user is selected in the user list

;; list of users
(s/def ::name ::user-name)
(s/def ::selected ::user-selected)
(s/def ::users
  (s/keys
   :req [::name]
   :opt [::selected]))

;; list of conversation participants
(s/def ::participants (s/coll-of ::user-name))

;; list of messages in conversation
(s/def ::from ::user-name)
(s/def ::text string?)
(s/def ::message
  (s/keys
   :req [::from ::text]))
(s/def ::messages (s/coll-of ::message))

;; conversation
(s/def ::conversation
  (s/keys
   :req [::participants ::messages]))

;; map of conversations
(s/def ::uuid
  (s/and
   keyword?
   #(uuid/valid-uuid? (name %))))  ; "name" turns keyword into string (without leading ":")
(s/def ::conversations (s/map-of ::uuid ::conversation))

;; app db
(s/def ::db
  (s/keys
   :req [::username ::users ::conversations ::error-messages]))

(def default-db
  {::username nil
   ::users #{}
   ::conversations {}
   ::error-messages []})
