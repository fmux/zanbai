(ns zanbai.server
  (:require [zanbai.handler :refer [handler dev-handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start-server [handler]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty handler {:port port :join? false})))

(defn dev-main []
  (start-server dev-handler))

(defn -main [& args]
  (start-server handler))
