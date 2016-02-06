(ns rss2json.core
  (:require [clojure.data.xml :as xml]
            [rss2json.parser :as parser]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as http]))

(defn content-type-from-http-headers [headers]
  (let [content-type-header (get headers "Content-Type")]    
    (->> (clojure.string/split content-type-header #";")
          (first))))

(defn is-xml? [content-type]
  (= "application/rss+xml" content-type))

(defn clean-nested-map
  "Remove all the nil values from a nested map"
  [nm]
  (clojure.walk/postwalk
    (fn [el]
      (if (map? el)
        (let [m (into (hash-map) (remove (comp nil? second) el))]
          (when (seq m) m))
        el)) nm))

(defn url->feed [url]
  (let [feed (parser/parse-feed-from-url url)]
    (json/generate-string
      (clean-nested-map feed) true)))

(defn render-json [status body]
  {:status status
   :body body
   :headers {"Content-Type" "application/json"}})
 
(defroutes app-routes
  (GET "/" {params :query-params}
       (if-let [feed (get params "feed")]
         (render-json 200 (url->feed feed))
         (render-json 200 "Missing feed")))
  (route/not-found "Not Found"))

(def app (-> app-routes handler/api))

(defn -main
  [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty app {:port port :join? false})))
