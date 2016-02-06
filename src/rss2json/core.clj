(ns rss2json.core
  (:require [clojure.data.xml :as xml]
            [rss2json.parser :as parser]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as http]))

(defn content-type-from-http-headers [headers]
  (let [content-type-header (get headers "Content-Type")]    
    (->> (clojure.string/split content-type-header #";")
          (first))))

(defn is-xml? [content-type]
  (= "application/rss+xml" content-type))

(defn rss-xml-from-url [url]
  (let [{status :status headers :headers body :body} (http/get url)
        content-type (content-type-from-http-headers headers)]
    (when (and (= 200 status) (is-xml? content-type))
      (xml/parse-str body))))

(defn url->feed [url]
  (let [feed (parser/parse-feed-from-url url)]
    (json/generate-string feed true)))

;; Web service
;; ---------------------------------------------------------------

(defn render-json [body]
  {:status 200
   :body body
   :headers {"Content-Type" "application/json"}})
 
(defroutes app-routes
  (GET "/" [] (render-json (url->feed "http://careers.stackoverflow.com/uk/jobs/feed")))
  (route/not-found "Not Found"))

(def app app-routes)
