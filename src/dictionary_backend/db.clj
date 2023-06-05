(ns dictionary-backend.db
  (:require [datalevin.core :as d]))

(def schema
  {:word/text #:db{:valueType :db.type/string
                   :unique :db.unique/identity}
   :word/type #:db{:valueType :db.type/keyword}
   :word/translations #:db{:valueType :db.type/ref
                          :cardinality :db.cardinality/many}
   :word/declensions #:db{:valueType :db.type/ref
                          :cardinality :db.cardinality/many}
   :word.declension/name #:db{:valueType :db.type/string}
   :word.declension/text #:db{:valueType :db.type/string}
   :word.declension/description #:db{:valueType :db.type/string}
   :word.declension.display/row #:db{:valueType :db.type/long}
   :word.declension.display/col #:db{:valueType :db.type/long}
   :word.declension.display/order #:db{:valueType :db.type/tuple
                                       :tupleAttrs [:word.declension.display/row
                                                    :word.declension.display/col]} ; where this should be positioned in a <table>
   })

(def db
  (-> (d/empty-db nil schema)
      (d/db-with [{:word/ ""}])))
