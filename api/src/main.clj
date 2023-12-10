(ns main
  (:require [ring.adapter.jetty :as jetty]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc])
  (:gen-class))

(defn get-port []
  (Integer/parseInt (System/getenv "PORT")))

(defn get-db-conf []
  {:dbtype  "postgres"
   :jdbcUrl (System/getenv "JDBC_DATABASE_URL")})

(defn migrations-config []
  {:db                   (get-db-conf)
   :store                :database
   :migration-dir        "migrations/"
   :migration-table-name "migrations"
   :init-in-transaction? false})

(defn create-migration [{:keys [name]}]
  (migratus.core/create (migrations-config)
                        name))

(defn datasource []
  (jdbc/get-datasource (get-db-conf)))

(defn app [_request]
  (let [db-version (jdbc/execute! (datasource) ["SELECT * FROM migrations"])]
    {:status  200
     :headers {"Content-Type" "application/edn"}
     :body    (str db-version)}))

(defn -main [& _args]
  ;; run migrations everytime the application starts
  (migratus/migrate (migrations-config))
  (jetty/run-jetty #'app {:port (get-port)}))
