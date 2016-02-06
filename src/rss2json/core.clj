(ns rss2json.core
  (:require [clojure.data.xml :as xml]
            [rss2json.parser :as parser]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as http]))

(defn content-type-from-http-headers [headers]
  (let [content-type-header (get headers "Content-Type")]    
    (->> (clojure.string/split content-type-header #";")
          (first))))

(defn is-xml? [content-type]
  (= "application/rss+xml" content-type))

(defn remove-nils
  "Remove all the nil values from a nested map"
  [nm]
  (clojure.walk/postwalk
   (fn [el]
     (if (map? el)
       (let [m (into {} (remove (comp nil? second) el))]
         (when (seq m)
           m))
       el))
      nm))

(defn url->feed [url]
  (let [feed (parser/parse-feed-from-url url)]
    (json/generate-string (remove-nils feed) true)))
                         
;; Web service
;; ---------------------------------------------------------------

(defn render-json [status body]
  {:status status
   :body body
   :headers {"Content-Type" "application/json"}})
 
(defroutes app-routes
  (GET "/" {params :query-params}
       (if-let [feed (get params "feed")]
         (render-json 200 (url->feed feed))
         (render-json 400 "Missing feed query param")))
  (route/not-found "Not Found"))

(def app (-> app-routes handler/api))
