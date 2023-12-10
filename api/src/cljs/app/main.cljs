(ns app.main
  (:require [app.utils :as utils]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [helix.core :refer [defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]))

(defnc App []
  (let [[data set-data] (hooks/use-state {})]
    (hooks/use-effect
     :once
     (-> (js/fetch "/api")
         (.then (fn [res] (. res text)))
         (.then (fn [response-string]
                  (set-data (edn/read-string response-string))))))
    (d/div
     (d/div "App Here")
     (d/pre (with-out-str
              (pprint data))))))

(defn init []
  (utils/render App))
