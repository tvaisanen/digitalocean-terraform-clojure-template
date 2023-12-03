(require '[cheshire.core :as json])

(def config
  (->  "3085b45633b1c0f87a3ea67000d7d77e2f14a74d1b2968966f866c7c3531f745.json"
       slurp
       (json/parse-string true)))

(keys config)
;; => (:architecture :config :created :history :os :rootfs)

(count (get config :history))
;; => 23

(for [{:keys [created_by]} (take-last 4 (get config :history))]
  created_by)
;; =>
;; ("WORKDIR /tmp/app"
;;  "COPY . . # buildkit"
;;  "RUN /bin/sh -c clojure -P # buildkit"
;;  "CMD [\"/bin/sh\" \"-c\" \"clj -X:run\"]")


(for [{:keys [created_by]} (take 3 (get config :history))]
  created_by)
;; =>
;; ("/bin/sh -c #(nop) ADD file:7c5789fb822bda2652d7addee832c5a3d71733f0f94f97d89b0c5570c0840829 in / "
;;  "/bin/sh -c #(nop)  CMD [\"bash\"]"
;;  "/bin/sh -c set -eux; \tapt-get update; \tapt-get install -y --no-install-recommends \t\tca-certificates \t\tcurl \t\tnetbase \t\twget \t; \trm -rf /var/lib/apt/lists/*")

(print (get-in config [:history 2 :created_by]))

(require '[clojure.string :as str])

(def install-commands
  (for [{:keys [created_by]} (get config :history)
        :when (str/includes? created_by "apt-get install")]
    created_by))

(count install-commands)
;; => 5


(for [command install-commands]
  (into []
        (comp
         (filter #(str/includes? % "apt-get install"))
         (map str/trim))
        (-> command
            (str/replace #"\t" "")
            (str/split #";"))))

;; (["apt-get install -y --no-install-recommends ca-certificates curl netbase wget"]
;;  ["apt-get install -y --no-install-recommends gnupg dirmngr"]
;;  ["/bin/sh -c apt-get update && apt-get install -y --no-install-recommends git mercurial openssh-client subversion procps && rm -rf /var/lib/apt/lists/*"]
;;  ["apt-get install -y --no-install-recommends bzip2 unzip xz-utils binutils fontconfig libfreetype6 ca-certificates p11-kit"]
;;  ["/bin/sh -c apt-get update && apt-get install -y make rlwrap && rm -rf /var/lib/apt/lists/* && wget https://download.clojure.org/install/linux-install-$CLOJURE_VERSION.sh && sha256sum linux-install-$CLOJURE_VERSION.sh && echo \"7677bb1179ebb15ebf954a87bd1078f1c547673d946dadafd23ece8cd61f5a9f *linux-install-$CLOJURE_VERSION.sh\" | sha256sum -c - && chmod +x linux-install-$CLOJURE_VERSION.sh && ./linux-install-$CLOJURE_VERSION.sh && rm linux-install-$CLOJURE_VERSION.sh && clojure -e \"(clojure-version)\""])
