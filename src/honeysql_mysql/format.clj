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

(def custom-clauses
  {:insert-ignore-into {:formatter insert-ignore-into-formatter
                        :before    :columns}})

(defn register-custom-clauses!
  []
  (doseq [[clause {:keys [formatter before]}] custom-clauses]
    (sql/register-clause! clause formatter before)))

(register-custom-clauses!)
