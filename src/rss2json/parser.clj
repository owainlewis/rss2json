(ns rss2json.parser
  (:import [com.sun.syndication.io SyndFeedInput XmlReader]
           [java.net URL]
           [java.io InputStreamReader]
           [com.sun.syndication.feed.synd SyndFeed]))

;; Code extracted+adapted from feedparser

(defrecord feed [
  authors
  categories
  contributors
  copyright
  description
  encoding
  entries
  feed-type
  image
  language
  link
  entry-links
  published-date
  title
  uri])

(defrecord entry [
  authors
  categories
  contents
  contributors
  description
  enclosures
  link
  published-date
  title
  updated-date
  url])

(defrecord enclosure [length type uri])
(defrecord person [email name uri])
(defrecord category [name taxonomy_uri])
(defrecord content [type value])
(defrecord image [description link title url])
(defrecord link [href hreflang length rel title type])

(defn- make-enclosure [e]
  (map->enclosure {:length (.getLength e) :type (.getType e) :url (.getUrl e)}))

(defn- make-content [c]
  (map->content {:type (.getType c) :value (.getValue c)}))

(defn- make-link [l]
  (map->link {:href (.getHref l)
              :hreflang (.getHreflang l)
              :length (.getLength l)
              :rel (.getRel l)
              :title (.getTitle l)
              :type (.getType l)}))

(defn- make-category [c] (map->category {:name (.getName c)}))

(defn- make-person [sp]
  (map->person {:email (.getEmail sp) :name (.getName sp) :uri (.getUri sp)}))

(defn- make-image [i]
  (map->image {:description (.getDescription i)
               :link (.getLink i)
               :title (.getTitle i)
               :url (.getUrl i)}))

(defn- make-entry [e]
  (map->entry {:authors (map make-person (seq (.getAuthors e)))
               :categories (map make-category (seq (.getCategories e)))
               :contents (map make-content (seq (.getContents e)))
               :contributors (map make-person (seq (.getContributors e)))
               :description (if-let [d (.getDescription e)] (make-content d))
               :enclosures (map make-enclosure (seq (.getEnclosures e)))
               :link (.getLink e)
               :published-date (.getPublishedDate e)
               :title (.getTitle e)
               :updated-date (.getUpdatedDate e)
               :uri (.getUri e)}))

(defn make-feed "Create a feed struct from a SyndFeed"
  [f]
  (map->feed  {:authors (map make-person (seq (.getAuthors f)))
               :categories (map make-category (seq (.getCategories f)))
               :contributors (map make-person (seq (.getContributors f)))
               :copyright (.getCopyright f)
               :description (.getDescription f)
               :encoding (.getEncoding f)
               :entries (map make-entry (seq (.getEntries f)))
               :feed-type (.getFeedType f)
               :image (if-let [i (.getImage f)] (make-image i))
               :language (.getLanguage f)
               :link (.getLink f)
               :entry-links (map make-link (seq (.getLinks f)))
               :published-date (.getPublishedDate f)
               :title (.getTitle f)
               :uri (.getUri f)}))

(defn- parse-internal [xmlreader]
   (let [syndfeed (.build (new SyndFeedInput) xmlreader)]
     (make-feed syndfeed)))

(defn parse-feed-from-url
  "Parse a feed from a given URL"
  [url]
  (parse-internal (new XmlReader (URL. url))))
