(ns honeysql-mysql.helpers
  (:require [honeysql.helpers :as h :refer [defhelper]]))

(defhelper insert-ignore-into [m fields]
  (assoc m :insert-ignore-into (first fields)))
