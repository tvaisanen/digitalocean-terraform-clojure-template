(ns main
  (:require [ring.adapter.jetty :as jetty]
            [next.jdbc :as jdbc])
  (:gen-class))

(defn get-port []
  (Integer/parseInt (System/getenv "PORT")))

(defn get-db-conf []
  {:dbtype "postgres"
   :jdbcUrl (System/getenv "JDBC_DATABASE_URL")})

(defn datasource []
  (jdbc/get-datasource (get-db-conf)))

(defn app [_request]
  (let [db-version (jdbc/execute! (datasource) ["SELECT version()"])]
    {:status  200
     :headers {"Content-Type" "application/edn"}
     :body    (str db-version)}))

(defn -main [& _args]
  (jetty/run-jetty #'app {:port (get-port)}))
