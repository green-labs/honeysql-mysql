(ns honeysql-mysql.format
  (:require [clojure.string :as string]
            [camel-snake-kebab.core :as csk]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defn insert-ignore-into-formatter
  [_op args]
  (let [insert-into (if (vector? args)
                      (apply h/insert-into args)
                      (h/insert-into args))
        insert-into' (-> insert-into
                         sql/format
                         first)
        result (string/replace insert-into' #"INSERT" "INSERT IGNORE")]
    [result]))

(defn explain-formatter
  [_op [explain-format]]
  (let [formats    #{:traditional
                     :json
                     :tree}
        format-sql (when (formats explain-format)
                     (str " FORMAT=" (sql/sql-kw explain-format)))]
    [(str "EXPLAIN" format-sql)]))

(defn match-against-formatter
  [_op [cols expr search-modifier]]
  (let [match        (str "("
                          (string/join ", " (map sql/format-entity cols))
                          ")")
        [against-sql & against-params] (sql/format-expr expr)
        modifiers    #{:in-natural-language-mode
                       :in-natural-language-mode-with-query-expansion
                       :in-boolean-mode
                       :with-query-expansion}
        modifier-sql (when (modifiers search-modifier)
                       (str " " (sql/sql-kw search-modifier)))]
    (-> [(str "MATCH " match " AGAINST (" against-sql modifier-sql ")")]
        (into against-params))))

(def custom-clauses
  {:insert-ignore-into {:formatter #'insert-ignore-into-formatter
                        :before    :columns}
   :explain            {:formatter #'explain-formatter
                        :before    :select}})

(def custom-fns
  {:match-against {:formatter #'match-against-formatter}})

(defn extend-syntax!
  []
  (doseq [[clause {:keys [formatter before]}] custom-clauses]
    (sql/register-clause! clause formatter before))

  (doseq [[f {:keys [formatter]}] custom-fns]
    (sql/register-fn! f formatter)))

(extend-syntax!)
