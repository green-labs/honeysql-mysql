(ns honey.sql.my.helpers
  (:require [honey.sql.helpers :as h]
            [honey.sql.my.format :as format]))

(defn insert-ignore-into
  [& args]
  (h/generic-helper-variadic :insert-ignore-into args))

(defn explain
  [& args]
  (h/generic-helper-variadic :explain args))
