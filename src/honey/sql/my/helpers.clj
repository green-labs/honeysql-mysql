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
