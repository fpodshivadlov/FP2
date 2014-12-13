(ns fp2.core
  (:gen-class)
  (:require [org.httpkit.client :as http]
            [clojure.string :as string])
)

(def options
  { :follow-redirects true }
)

(defn getValuesFromMatcher [matcher values]
  (let [value (re-find matcher)
        href (second value)]
    (if
      (not= value nil)
      (recur matcher (conj values href))
      values
    )
  )
)

(defn getUrlsFromBody
  [html]
  (let [matcher (re-matcher #"<a[^<>]*href=\"(?<href>[^<>\"]*)\"[^<>]*>" html)]
    (getValuesFromMatcher matcher [])
  )
)

(defn crawle
  [result urls depth maxDepth]
  (if
    (< depth maxDepth)
    (doall
      (pmap
        (fn [url]
          (let [responce @(http/get url options)
                url (-> responce :opts :url)
                body (:body responce)
                status (:status responce)
                error (:error responce)
                offsetString (apply str (repeat depth "\t"))
                redirectUrl (-> responce :opts :trace-redirects first)
                redirectString (if (not= redirectUrl nil) (str " redirect " redirectUrl) "")]
            (cond
              (= status 200)
                (let [newUrls (getUrlsFromBody body)
                      inner (atom [])]
                  (crawle inner newUrls (inc depth) maxDepth)
                  (swap!
                    result
                    conj
                    { :success true
                      :url url
                      :depth depth
                      :countUrls (count newUrls)
                      :redirectUrl redirectUrl
                      :inner @inner
                    }
                  )
                )
              (or error (>= status 400))
                (swap!
                  result
                  conj
                  { :success false
                    :url url
                    :depth depth
                  }
                )
            )
          )
        )
        urls
      )
    )
  )
)

(defn formatResult [data]
  (apply
    str
    (map
      (fn [entry]
        (let [url (:url entry)
              success (:success entry)
              depth (:depth entry)
              offsetString (apply str (repeat depth "\t"))
              redirectUrl (:redirectUrl entry)
              redirectString (if (not= redirectUrl nil) (str " redirect " redirectUrl) "")]
          (if
            success
            (str offsetString url " " (:countUrls entry) " links" redirectString "\n"
              (formatResult (:inner entry))
            )
            (str offsetString url " bad" "\n")
          )
        )
      )
      data
    )
  )
)

(defn -main
  "The program"
  [filename depth & args]
  (if
    (.exists (clojure.java.io/as-file filename))
    (let [urls (string/split (slurp filename) #"\r\n")
          result (atom [])]
      (crawle result urls 0 (Integer/parseInt depth))
      (print (formatResult @result))
    )
    (println "file isn't exist:" filename)
  )
)

;(-main "task/links.txt" "2")
