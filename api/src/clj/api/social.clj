(ns social
  (:require [domain :as domain]
            [db :as db]
            [malli.core :as m]
            [malli.error :as me]
            [malli.transform :as mt]))

(defn db->Post [post]
  (->>
   (into {} (for [{:keys [platform data]} (:content post)]
              {platform (dissoc data :platform)}))
   (assoc post :content)
   (domain/decode domain/Post)))

(def transformer
  (mt/default-value-transformer
   {:key      :default
    :defaults {:map    (constantly {})
               :string (constantly "")
               :vector (constantly [])}}))

(defn Post->LinkedInShare [{:keys [author content] :as post}]

  (when-not (m/validate domain/Post post)
    (throw
     (ex-info "Invalid Post"
              {:data post
               :error (me/humanize
                       (m/explain domain/Post post))})))

  (let [payload (-> (m/decode domain/LinkedInPost nil transformer)
                    (assoc :author author)
                    (assoc-in [:specificContent :com.linkedin.ugc.ShareContent
                               :shareCommentary :text]

    (when-not (m/validate domain/LinkedInPost payload)
      (throw (ex-info "Invalid LinkedIn text share"
                      {:data  payload
                       :error (me/humanize
                               (m/explain domain/LinkedInPost payload))})))

    payload))

;;
;; SQL
;;

(defn posts-to-schedule [db]
  (let [query [(str "select p.*, json_agg(c) as content"
                    "  from post p"
                    "  left join content c using (post_id)"
                    "  where p.scheduled <= ?"
                    "    and p.status = 'SCHEDULED'"
                    "    and c.data is not null"
                    "  group by p.post_id")
               (java.time.Instant/now)]]
    (->> query
         (db/query db)
         (map db->Post))))

(defn get-post [db {:keys [post-id]}]
  (let [query [(str "select p.*, json_agg(c) as content"
                    "  from post p"
                    "  left join content c using (post_id)"
                    "  where p.post_id = ?"
                    "  group by p.post_id")
               post-id]]
    (->> query
        (db/query db)
        (first)
        (db->Post))))

(defn get-posts [db]
  (let [query [(str "select p.*, json_agg(c) as content"
                    "  from post p"
                    "  left join content c using (post_id)"
                    "  group by p.post_id")]]
    (->> query
         (db/query db)
         (map db->Post))))

(defn publish-scheduled-posts
  "Use this as a placeholder"
  [db]
  (let [posts (posts-to-schedule db)]
    (when (seq posts)
      ;; publish
      (->> posts
           (map (fn [p]
                  {:url  "LINKEDIN"
                   :body (Post->LinkedInShare p)}))
           (db/insert-multi! db :dev_publications)))))

(comment

  (posts-to-schedule (db/datasource))

  (publish-scheduled-posts (db/datasource))

  (def cron
    (.start (Thread/ofVirtual)
            (fn []
              (loop [_ nil]
                (prn "Check scheduled...")
                (let [to-post (posts-to-schedule (db/datasource))]
                  (prn to-post))
                (Thread/sleep 10000)
                (recur nil)))))

  (.interrupt cron))

(defn publish-post [db post]
  (db/insert! db :dev-publications
              {:url "https://api.linkedin.com/v2/ugcPosts"
               :body (Post->LinkedInShare post)}))


(comment
  (get-posts (db/datasource)))

(defn create-post! [db {:keys [author status scheduled content]}]
  (let [post         (db/insert! db :post {:author    author
                                           :scheduled scheduled
                                           :status status})
        content-rows (into []
                           (for [[platform data] content]
                             {:post-id  (get post :post-id)
                              :author   author
                              :platform (name platform) ;; keyword
                              :data     data}))
        content-rows (db/insert-multi! db :content content-rows)
        content (into {}
                      (for [{:keys [platform data]} content-rows]
                          {platform data}))]

    (domain/decode domain/Post
                   (assoc post :content content))))

(comment
  (create-post! (db/datasource)
                {:author    "test-user"
                 #_#_:scheduled (java.time.Instant/now)
                 :scheduled (.toInstant #inst "2023-12-31")
                 :status    "SCHEDULED"
                 :content   {:linkedin {:text "Linkedin Post"}
                             :twitter {:text "twwwtteeet"}}}))
