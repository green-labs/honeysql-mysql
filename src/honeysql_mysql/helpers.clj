(ns honeysql-mysql.helpers
  (:require [honey.sql.helpers :as h]
            [honey.sql :as sql]
            [honeysql-mysql.format :as format]))

(defn insert-ignore-into
  [& args]
  (h/generic-helper-variadic :insert-ignore-into args))

(defn explain
  [& args]
  (h/generic-helper-variadic :explain args))
