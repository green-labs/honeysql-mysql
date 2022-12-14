(ns honey.sql.my.helpers
  (:require [honey.sql.helpers :as h]
            [honey.sql.my.format :as format]))

(defn insert-ignore-into
  [& args]
  (h/generic-helper-variadic :insert-ignore-into args))

(defn explain
  [& args]
  (h/generic-helper-variadic :explain args))

(defn select-with-optimizer-hints
  "Only supports Index-Level Optimizer Hints yet
   TODO) Support other levels optimizer hints"
  [& args]
  (h/generic-helper-variadic :select-with-optimizer-hints args))

(defn values-as
  [& args]
  (h/generic-helper-variadic :values-as args))

(defn select-straight-join
  [& args]
  (h/generic-helper-variadic :select-straight-join args))
