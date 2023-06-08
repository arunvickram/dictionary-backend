(ns dictionary-backend.db
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]))

(def schema
  [#:db{:ident :word/text
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :fulltext true
        :doc "The base word's textual representation."}

   #:db{:ident :word/lang
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :doc "The language that this word belongs to in ISO 639-3 format"}

   #:db{:ident :word/type
        :cardinality :db.cardinality/one
        :valueType :db.type/keyword
        :doc "The type of the word: verb, adjective, etc."}

   #:db{:ident :word/translations
        :valueType :db.type/ref
        :cardinality :db.cardinality/many
        :doc "The list of words that correspond to this"}

   #:db{:ident :word/inflections
        :valueType :db.type/ref
        :cardinality :db.cardinality/many
        :doc "List of inflections for the word"}

   #:db{:ident :word/examples
        :valueType :db.type/ref
        :cardinality :db.cardinality/many
        :doc "List of examples for this particular word"}

   #:db{:ident :word/variants
        :valueType :db.type/ref
        :cardinality :db.cardinality/many
        :doc "List of stylistic variants of the particular base word"}

   #:db{:ident :word.inflection/text
        :cardinality :db.cardinality/many
        :valueType :db.type/string
        :doc "The textual representation of the inflected form of the word."}

   #:db{:ident :word.inflection/eng-representation
        :cardinality :db.cardinality/many
        :valueType :db.type/string
        :doc "The english representation of the inflection."}

   #:db{:ident :word.inflection/tags
        :valueType :db.type/ref
        :cardinality :db.cardinality/many}

   #:db{:ident :word.inflection/tags-id
        :valueType :db.type/tuple
        :cardinality :db.cardinality/one
        :tupleType :db.type/keyword}

   #:db{:ident :word.inflection/root-ref
        :valueType :db.type/ref
        :cardinality :db.cardinality/one}

   #:db{:ident :word.inflection/root
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :doc "The root of the word."}

   #:db{:ident :word.inflection/prefix
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :doc "The prefixes that would be added to modify this word"}

   #:db{:ident :word.inflection/suffix
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :doc "The suffixes that would be added to modify this word"}

   #:db{:ident :word.inflection.tag/text
        :cardinality :db.cardinality/one
        :valueType :db.type/string
        :doc "The human readable representation of this inflection tag"}

   #:db{:ident :word.inflection.tag/name
        :cardinality :db.cardinality/one
        :valueType :db.type/keyword
        :unique :db.unique/identity
        :doc "The internal name of the tag"}])

(defstate datomic
  :start (do
           (def db-uri "datomic:mem://hello")
           (d/create-database db-uri)
           (d/connect db-uri)))

(defn get-word [word lang]
  (d/q '[:find (pull ?e [:* #:word{:translations [:*]
                                   :inflections [:* {:word.inflection/tags [:word.inflection.tag/name
                                                                            :word.inflection.tag/text]}]}]) .
         :in $ ?word ?lang
         :where
         [(fulltext $ :word/text ?word) [[?e ?text]]]
         [?e :word/lang ?lang]]
       (d/db datomic)
       word
       lang))

(defn get-declension-for-word [word tags]
  (d/q '[:find (pull ?e [:*]) .
         :in $ ?word ?tags
         :where
         [?e :word.inflection/root ?word]
         [?e :word.inflection/tags-id ?tags]
         [?e :word/text ?text]]
       (d/db datomic)
       word
       tags))

(comment

  @(d/transact datomic schema)

  (first schema)


  @(d/transact datomic
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
                 :word.inflection/tags-id [:tense/past :person/first :number/singular]
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
                 :word.inflection/tags-id [:tense/present :person/first :number/singular]
                 :word.inflection/eng-representation "I am"
                 :word.inflection/root "இரு"
                 :word.inflection/suffix "க்கிறேன்"}])


  (get-declension-for-word (d/db conn) "இரு" [:tense/present :person/first :number/singular])

  (d/q '[:find (pull ?e ["*"])
         :in $ ?tag
         :where
         [?e :word.inflection.tag/text ?tag]]
       (d/db conn)
       "Past tense")


  )
