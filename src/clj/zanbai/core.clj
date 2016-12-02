(ns zanbai.core
  (:require [clj-uuid :as uuid]))

(def app-state
  (atom {:users ()
         :conversations {}}))

(defn send-message [from conversation text]
  (loop []
    (let [current-state @app-state
          users (:users current-state)
          participants (remove #(-> current-state (get-in [:conversations % conversation]) nil?) users)
          other-participants (filter #(not= from %) participants)
          new-state (reduce (fn [state participant] (update-in state [:conversations participant conversation] #(conj % {:from from :text text}))) current-state other-participants)]
      ; TODO: this one we can do with swap!
      (do
        (println "from" from "conversation" conversation "tex" text "current-state" current-state "users" users "participants" participants "other-participants" other-participants "new-state" new-state)
        (if (compare-and-set! app-state current-state new-state)
          (println "send-message" from conversation text app-state)
          (recur))))))

(defn get-pending-messages [user]
  (loop []
    (let [current-state @app-state
          map-path [:conversations user]
          conversations-of-user (get-in current-state map-path)
          new-state (reduce #(assoc-in %1 (conj map-path %2) []) current-state (keys conversations-of-user))]
      (if (compare-and-set! app-state current-state new-state)
        conversations-of-user
        (recur)))))
