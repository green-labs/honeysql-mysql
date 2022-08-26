(ns honeysql-mysql.helpers
  (:require [honeysql-mysql.format :as fmt]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defn insert-ignore-into
  [& args]
  (h/generic-helper-variadic :insert-ignore-into args))
