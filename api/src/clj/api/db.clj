(ns api.db
  (:require [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            ;; [next.jdbc.result-set :as rs]
            ;; [next.jdbc.sql :as sql]
            [next.jdbc.date-time]
            ;; [next.jdbc.prepare :as prepare]
            ;; [jsonista.core :as json]

            )
  (:import (org.postgresql.util PGobject)
           (java.sql PreparedStatement)))

;; (def mapper (json/object-mapper {:decode-key-fn keyword}))
;; (def ->json json/write-value-as-string)
;; (def <-json #(json/read-value % mapper))

;; (defn ->pgobject
;;   "Transforms Clojure data to a PGobject that contains the data as
;;   JSON. PGObject type defaults to `jsonb` but can be changed via
;;   metadata key `:pgtype`"
;;   [x]
;;   (let [pgtype (or (:pgtype (meta x)) "jsonb")]
;;     (doto (PGobject.)
;;       (.setType pgtype)
;;       (.setValue (->json x)))))

;; (defn <-pgobject
;;   "Transform PGobject containing `json` or `jsonb` value to Clojure
;;   data."
;;   [^org.postgresql.util.PGobject v]
;;   (let [type  (.getType v)
;;         value (.getValue v)]
;;     (if (#{"jsonb" "json"} type)
;;       (when value
;;         (with-meta (<-json value) {:pgtype type}))
;;       value)))

;; (set! *warn-on-reflection* true)

;; ;; if a SQL parameter is a Clojure hash map or vector, it'll be transformed
;; ;; to a PGobject for JSON/JSONB:
;; (extend-protocol prepare/SettableParameter
;;   clojure.lang.IPersistentMap
;;   (set-parameter [m ^PreparedStatement s i]
;;     (.setObject s i (->pgobject m)))

;;   clojure.lang.IPersistentVector
;;   (set-parameter [v ^PreparedStatement s i]
;;     (.setObject s i (->pgobject v))))

;; ;; if a row contains a PGobject then we'll convert them to Clojure data
;; ;; while reading (if column is either "json" or "jsonb" type):
;; (extend-protocol rs/ReadableColumn
;;   org.postgresql.util.PGobject
;;   (read-column-by-label [^org.postgresql.util.PGobject v _]
;;     (<-pgobject v))
;;   (read-column-by-index [^org.postgresql.util.PGobject v _2 _3]
;;     (<-pgobject v)))

;; (def defaults
;;   (assoc jdbc/snake-kebab-opts
;;          :builder-fn rs/as-unqualified-kebab-maps))

;; (defn query [db query]
;;   (sql/query db query defaults))

;; (defn insert! [db table data]
;;   (sql/insert! db table data defaults))

;; (defn insert-multi! [db table data]
;;   (sql/insert-multi! db table data defaults))

;; (defn delete! [db table where]
;;   (sql/delete! db table where defaults))

;; (defn find-by-keys
;;   ([db table where]
;;    (find-by-keys db table where {}))
;;   ([db table where opts]
;;    (sql/find-by-keys db table where (merge defaults opts))))

;; (defn update-returning [db table values where]
;;   (sql/update! db table values where (assoc defaults :suffix "RETURNING *")))

(def dev-jdbc-url
  "jdbc:postgresql://localhost:5432/db?user=user&password=password")

(defn get-db-conf []
  {:dbtype  "postgres"
   :jdbcUrl (or (System/getenv "JDBC_DATABASE_URL")
                dev-jdbc-url)})

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

(defn get-migrations []
  (jdbc/execute! (datasource) ["SELECT * FROM migrations"]))

(defn run-migrations []
  (migratus/migrate (migrations-config)))

(comment
  (migratus/migrate (migrations-config))
  (migratus/rollback (migrations-config))
  (migratus/reset (migrations-config))
  (migratus/create (migrations-config) "schemas"))
