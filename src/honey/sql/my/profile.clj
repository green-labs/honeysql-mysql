(ns honey.sql.my.profile
  (:require [honey.sql :as sql]
            [honey.sql.my.format :as format]))

(defn format [data opts]
  (tap> (honey.sql/format (merge data {:explain []}) opts))
  (honey.sql/format data opts))
