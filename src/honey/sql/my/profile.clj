(ns honey.sql.my.profile
  (:require [honey.sql :as sql]
            [honey.sql.my.format :as format]))

(defn custom-format
  [format]
  (fn [data & args]
    (tap> (apply format (merge data {:explain []}) args))
    (apply format data args)))
