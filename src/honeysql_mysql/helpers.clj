(ns honeysql-mysql.helpers
  (:require [honey.sql.helpers :as h]))

(defn insert-ignore-into
  [& args]
  (h/generic-helper-variadic :insert-ignore-into args))
