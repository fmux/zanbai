(defproject zanbai "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [compojure "1.5.0"]
                 [yogthos/config "0.8"]
                 [day8.re-frame/http-fx "0.1.2"]
                 [danlentz/clj-uuid "0.1.6"]
                 [ring/ring-json "0.4.0"]
                 [ring "1.4.0"]]

  :plugins [[lein-cljsbuild "1.1.4"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]
  :test-paths ["tests/clj"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler zanbai.handler/dev-handler}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-sidecar "0.5.2"]]
    :source-paths ["src/cljs"]
    :plugins      [[lein-figwheel "0.5.7"]]
    }}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "zanbai.core/mount-root"}
     :compiler     {:main                 zanbai.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            zanbai.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                 :init (-main)}

  :main zanbai.server

  :aot [zanbai.server]

  :uberjar-name "zanbai.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "compile"]
  )
