(defproject rolodex-gui "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.225"]
                 [clj-http "2.2.0"]
                 [ring/ring-core "1.3.2"]
                 [com.stuartsierra/component "0.3.1"]
                 [figwheel-sidecar "0.5.4-2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [hawk "0.2.10"]

                 [garden "1.3.2"]
                 [reagent "0.6.0-rc"]
                 [cljs-ajax "0.5.8"]]

  :source-paths ["src" "dev"]

  :plugins [[lein-figwheel "0.5.4-2"]]

  :figwheel {:ring-handler rolodex.devserver/api-proxy
             :css-dirs ["resources/public/css"]}

  :main rolodex.devserver

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :cljsbuild
  {:builds [{:id "dev"
             :figwheel true
             :source-paths ["src"]
             :compiler
             {:main rolodex.gui
              :output-dir "resources/public/js/compiled"
              :output-to  "resources/public/js/compiled/gui.js"
              :asset-path "js/compiled"
              :optimizations :none}}

            {:id "prod"
             :source-paths ["src"]
             :compiler
             {:output-to "resources/public/js/compiled/gui.js"
              :optimizations :advanced}}]})
