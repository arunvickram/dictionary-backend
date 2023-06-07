(ns dictionary-backend.db-test
  (:require [dictionary-backend.db :as sut]
            [datomic.api :as d]
            [clojure.test :as t :refer [are deftest is testing]]))

(def sample-data
  [{:db/id -1
                 :word/type :verb
                 :word/text "இரு"
                 :word/lang "tam"
                 :word/translations [-2]
                 :word/inflections [-6 -8]}
                {:db/id -2
                 :word/type :verb
                 :word/text "be"
                 :word/lang "eng"}
                {:db/id -3
                 :word.inflection.tag/text "Past tense"
                 :word.inflection.tag/name :tense/past}
                {:db/id -4
                 :word.inflection.tag/text "1st person"
                 :word.inflection.tag/name :person/first}
                {:db/id -5
                 :word.inflection.tag/text "Plural"
                 :word.inflection.tag/name :number/plural}
                {:db/id -6
                 :word/type :verb
                 :word/text "இருந்தேன்"
                 :word/lang "tam"
                 :word.inflection/tags [-3 -4]
                 :word.inflection/eng-representation "I was"
                 :word.inflection/root-ref -1
                 :word.inflection/root "இரு"
                 :word.inflection/suffix "ந்தேன்"}
                {:db/id -7
                 :word.inflection.tag/text "Present tense"
                 :word.inflection.tag/name :tense/present}
                {:db/id -8
                 :word/type :verb
                 :word/text "இருக்கிறேன்"
                 :word/lang "tam"
                 :word.inflection/tags [-7 -4]
                 :word.inflection/eng-representation "I am"
                 :word.inflection/root "இரு"
                 :word.inflection/suffix "க்கிறேன்"}])

(deftest fetch-word-information
  (let [_ @(d/transact sut/conn sut/schema)
        _ @(d/transact sut/conn sample-data)]

    (testing "Fetches basic information about the word"
      (let [data (sut/get-word (d/db sut/conn) "இரு" "tam")]
        (is (not= data nil))))))

(comment
  (require '[kaocha.repl :as k])

  @(d/transact sut/conn sut/schema)

  (k/run *ns*)

  )
