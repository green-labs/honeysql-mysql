(ns honey.sql.my.format
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

(def index-level-optimizer-hint-names
  #{:group-index
    :no-group-index
    :index
    :no-index
    :index-merge
    :no-index-merge
    :join-index
    :no-join-index
    :mrr
    :no-mrr
    :no-icp
    :no-range-optimization
    :order-index
    :no-order-index
    :skip-scan
    :no-skip-scan})

(defn hint->hint-format-string
  "Turns a hint to MySQL optimizer hint format string
   ex) [:group-index tb1 [i_a i_b]]
       => GROUP_INDEX(tb1 i_a, i_b)"
  [[hint-name table indexes]]
  (format "%s(%s %s)"
          (csk/->SCREAMING_SNAKE_CASE_STRING hint-name)
          table
          (string/join ", " indexes)))

(defn select-with-optimizer-hints-formatter
  [_op [cols hints]]
  (let [hints      (->> hints
                        (filter (fn [[hint-name _ _]] (index-level-optimizer-hint-names hint-name)))
                        (map hint->hint-format-string)
                        (string/join " "))
        hint-sql   (str "/*+ " hints " */")
        select-sql (-> (apply h/select cols)
                       sql/format)]
    (update select-sql 0 #(string/replace % #"SELECT" (str "SELECT " hint-sql)))))

(def custom-clauses
  {:insert-ignore-into          {:formatter #'insert-ignore-into-formatter
                                 :before    :columns}
   :explain                     {:formatter #'explain-formatter
                                 :before    :select}
   :select-with-optimizer-hints {:formatter #'select-with-optimizer-hints-formatter
                                 :before    :from}})

(def custom-fns
  {:match-against {:formatter #'match-against-formatter}})

(defn extend-syntax!
  []
  (doseq [[clause {:keys [formatter before]}] custom-clauses]
    (sql/register-clause! clause formatter before))

  (doseq [[f {:keys [formatter]}] custom-fns]
    (sql/register-fn! f formatter)))

(extend-syntax!)
