(ns zanbai.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [zanbai.events]
              [zanbai.subs]
              [zanbai.views :as views]
              [zanbai.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  ;(do
    (reagent/render [views/app]
                    (.getElementById js/document "app")))
  ;  (reagent/render [views/error-messages]
  ;                  (.getElementById js/document "error-messages")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
