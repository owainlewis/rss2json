(defproject rss2json "0.1.0-SNAPSHOT"
  :description "A simple web service that turns RSS feed XML into JSON"
  :url "http://github.com/owainlewis/rss2json"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler rss2json.core/app}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [compojure "1.4.0"]
                 [clj-http "2.0.1"]
                 [rome/rome "1.0"]
                 [cheshire "5.5.0"]])
