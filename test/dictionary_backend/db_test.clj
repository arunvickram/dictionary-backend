(ns dictionary-backend.db-test
  (:require [dictionary-backend.db :as sut]
            [clojure.test :as t :refer [are deftest is testing]]))

(deftest fetch-word-information
  (testing "Fetches basic information about the word"
    (is (= 0 1)))
  (testing "Fetches declension information about a particular word"
    (is (= 0 1))))
