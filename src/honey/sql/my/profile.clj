(ns honey.sql.my.profile
  (:require [honey.sql :as sql]))

(defn format [data opts]
  (tap> (honey.sql/format (merge data {:explain []}) opts))
  (honey.sql/format data opts))
