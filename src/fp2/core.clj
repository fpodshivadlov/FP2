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
  [urls depth maxDepth]
  (if
    (< depth maxDepth)
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
                (let [newUrls (getUrlsFromBody body)]
                  { :success true
                    :url url
                    :depth depth
                    :countUrls (count newUrls)
                    :redirectUrl redirectUrl
                    :inner (crawle newUrls (inc depth) maxDepth)
                  }
                )
              (or error (>= status 400))
                { :success false
                  :url url
                  :depth depth
                }
            )
          )
        )
        urls
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
          data (crawle urls 0 depth)]
      (print (formatResult data))
    )
    (println "file isn't exist:" filename)
  )
)

;(-main "task/links.txt" 2)
