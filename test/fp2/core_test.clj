(ns fp2.core-test
  (:require [clojure.test :refer :all]
            [fp2.core :refer :all]))

(def html1 "<html><body><a href=\"link1\">text</a></body><html>")
(def html2 "<html><body><a href=\"link1\" class=\"black\">text</a></body><html>")
(def html3 "<html><body><a href=\"link1\" class=\"black\">text</a><a id=\"i347\" href=\"link2\" class=\"black\">text</a></body><html>")

(deftest parsing-test
  (testing "parsing html1"
    (is (= (count (getUrlsFromBody html1)) 1))
    (is (= (getUrlsFromBody html1) ["link1"]))
  )
  (testing "parsing html2"
    (is (= (count (getUrlsFromBody html2)) 1))
    (is (= (getUrlsFromBody html2) ["link1"]))
  )
  (testing "parsing html3"
    (is (= (count (getUrlsFromBody html3)) 2))
    (is (= (getUrlsFromBody html3) ["link1", "link2"]))
  )
)

(deftest crawle-test
  (testing "parsing html1"
    (is (let [result (atom [])]
      (crawle result ["http://ya.ru"] 0 1)
      (= (-> @result first :success) true)
    ))
    (is (let [result (atom [])]
      (crawle result ["http://pageisnotexist"] 0 1)
      (= (-> @result first :success) false)
    ))
  )
)
