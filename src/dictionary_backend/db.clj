(ns dictionary-backend.db
  (:require [datalevin.core :as d]))

(def schema
  {:word/text #:db{:valueType :db.type/string
                   :unique :db.unique/identity}
   :word/translations #:db{:valueType :db.type/ref
                          :cardinality :db.cardinality/many}
   :word/type #:db{:valueType :db.type/keyword}
   :word/declensions #:db{:valueType :db.type/ref
                          :cardinality :db.cardinality/many}
   :word.declension/description #:db{:valueType :db.type/string}
   :word.declension/table-order #:db{:valueType :db.type/tuple} ; where this should be positioned in a <table>
   :word.declension/name #:db{:valueType :db.type/string}
   :word.declension/text #:db{:valueType :db.type/string}})

(def db
  (-> (d/empty-db nil schema)))
