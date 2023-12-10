(ns api.main
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [api.db :as db])
  (:gen-class))

(defn get-port []
  (Integer/parseInt (or (System/getenv "PORT")
                        "8000")))

(defn handler [_request]
  (let [migrations (db/get-migrations)]
    {:status  200
     :headers {"Content-Type" "application/edn"}
     :body    (str migrations)}))

(defn index-handler
  [_]
  {:status  200
   :headers {"content-type" "text/html"}
   :body    (io/file (io/resource "public/index.html"))})

(def app
  (ring/ring-handler
   (ring/router
    [["/api"
      ["*" handler]]
     ["/" index-handler]
     ["/*" (ring/create-resource-handler
            {:not-found-handler
             (fn [{:keys [uri] :as r}]
               (if (str/starts-with? uri "/api")
                 {:status 404}
                 (index-handler r)))})]]
    {:conflicts (constantly nil)})
   (ring/create-default-handler)))

(defn start! [opts]
  (db/run-migrations)
  (jetty/run-jetty #'app opts))

(defn -main [& _args]
  (start! {:port (get-port)}))

(comment
  (require '[clojure.java.io :as io])

  (for [resource (concat (file-seq (io/file (io/resource "public")))
                         (file-seq (io/file (io/resource "public/js"))))]
    (.getName resource)))
