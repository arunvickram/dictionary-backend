(ns dictionary-backend.db
  (:require [datalevin.core :as d]
            [dictionary-backend.db :as db]))

(def schema
  {:word/text #:db{:valueType :db.type/string
                   :fulltext true
                   :unique :db.unique/identity}
   :word/iso693-3 #:db{:valueType :db.type/string}
   :word/type #:db{:valueType :db.type/keyword}
   :word/translations #:db{:valueType :db.type/ref
                           :cardinality :db.cardinality/many}
   :word/declensions #:db{:valueType :db.type/ref
                          :cardinality :db.cardinality/many}

   :word/variants #:db{:valueType :db.type/ref
                       :cardinality :db.cardinality/many} ; these are simply stylistic variants of the word
   :word.declension/eng-representation #:db{:valueType :db.type/string}
   :word.declension/text #:db{:valueType :db.type/string} ; the affixes to add
   :word.declension/tags #:db{:valueType :db.type/ref
                              :cardinality :db.cardinality/many}
   :word.declension/root #:db{:valueType :db.type/ref
                              :cardinality :db.cardinality/one}

   :word.declension.tag/text #:db{:valueType :db.type/string}
   :word.declension.tag/name #:db{:valueType :db.type/keyword
                                  :unique :db.unique/identity}
   })

(def conn (d/get-conn "/tmp/datalevin/dictionary" schema))

(defn get-word [db word]
  (d/q '[:find (pull ?e ["*" #:word{:translations ["*"]
                                    :declensions ["*" {:word.declension/tags [:word.declension.tag/name
                                                                              :word.declension.tag/text]}]}])
         :in $ ?text
         :where
         [?e :word/text ?text]]
       db
       word))

(defn get-declension-for-word [db word tags]
  (d/q '[:find ?text
         :in $ ?word ?tags
         :where
         [?e :word.declension/root ?word]
         [?e :word.declension/tags ]
         [?e :word/text ?text]]
       db
       word
       tags))

(comment
  (d/transact! conn
               [{:db/id -1
                 :word/type :verb
                 :word/text "இரு"
                 :word/iso693-3 "tam"
                 :word/translations [-2]
                 :word/declensions [-6 -8]}
                {:db/id -2
                 :word/type :verb
                 :word/text "be"
                 :word/iso693-3 "eng"}
                {:db/id -3
                 :word.declension.tag/text "Past tense"
                 :word.declension.tag/name :tense/past}
                {:db/id -4
                 :word.declension.tag/text "1st person"
                 :word.declension.tag/name :first-person}
                {:db/id -5
                 :word.declension.tag/text "Plural"
                 :word.declension.tag/name :plural}
                {:db/id -6
                 :word/type :verb
                 :word/text "இருந்தேன்"
                 :word/
                 :word/iso693-3 "tam"
                 :word.declension/tags [-3 -4]
                 :word.declension/eng-representation "I was"
                 :word.declension/root -1}
                {:db/id -7
                 :word.declension.tag/text "Present tense"
                 :word.declension.tag/name :tense/present}
                {:db/id -8
                 :word/type :verb
                 :word/text "இருக்கிறேன்"
                 :word/iso693-3 "tam"
                 :word.declension/tags [-7 -4]
                 :word.declension/eng-representation "I am"
                 :word.declension/root -1}])


  (def db
    (-> (d/empty-db nil schema)
        (d/db-with [{:db/id -1
                     :word/type :verb
                     :word/text "இருத்தல்"
                     :word/translations [-2]}
                    {:db/id -2
                     :word/type :verb
                     :word/text "to be"}
                    {:db/id -3
                     :word/text "இருந்தேன்"}
                    {}])))

  (d/entries db "o")


  (get-word (d/db conn) "இரு")


  (d/q '[:find (pull ?e ["*"])
         :in $ ?tag
         :where
         [?e :word.declension.tag/text ?tag]]
       (d/db conn)
       "Past tense")


  )
