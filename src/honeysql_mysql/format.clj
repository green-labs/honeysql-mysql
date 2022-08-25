(ns honeysql-mysql.format
  (:require [honeysql.format :as fmt]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [honeysql-mysql.helpers :as mysqlh]))

(def custom-clause-priorities
  {:insert-ignore-into (:insert-into fmt/default-clause-priorities)})

(defn register-custom-clauses!
  []
  (doseq [[clause-key priority] custom-clause-priorities]
    (fmt/register-clause! clause-key priority)))

(defmethod fmt/format-clause :insert-ignore-into [[_op table-name] _sqlmap]
  (str "INSERT IGNORE INTO " (fmt/to-sql table-name)))

(register-custom-clauses!)

(comment
  custom-clause-priorities

  (sql/format {:insert-ignore-into :some-table
               :columns            [:col1 :col2]
               :values             [[1 2] [3 4]]})
  (sql/format (-> (mysqlh/insert-ignore-into :some-table)
                  (h/columns :col1 :col2)
                  (h/values [[1 2] [3 4]])))
  ;; => ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
  )

