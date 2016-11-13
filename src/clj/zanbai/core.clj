(ns zanbai.core)

(def app-state (atom {
  :users ()
  :conversations {}
}))

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

