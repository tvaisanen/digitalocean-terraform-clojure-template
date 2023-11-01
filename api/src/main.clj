(ns main
  (:require [ring.adapter.jetty :as jetty]
            [next.jdbc :as jdbc])
  (:gen-class))

(def port (Integer/parseInt (System/getenv "PORT")))

(def db {:dbtype "postgres"
         :jdbcUrl (System/getenv "JDBC_DATABASE_URL")})

(def ds (jdbc/get-datasource db))

(defn app [_request]
  (let [db-version (jdbc/execute! ds ["SELECT version()"])]
    {:status  200
     :headers {"Content-Type" "application/edn"}
     :body    (str db-version)}))

(defn run! [& _args]
  (jetty/run-jetty #'app {:port port}))
