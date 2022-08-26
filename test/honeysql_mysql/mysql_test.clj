(ns honeysql-mysql.mysql-test
  (:require [clojure.test :refer [deftest run-tests is testing]]
            [honeysql-mysql.helpers :as mysql-h]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(h/insert-into)
(deftest insert-ignore-into-test
  (testing "INSERT IGNORE INTO sql generation for mysql"
    (is (= ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
           (-> (mysql-h/insert-ignore-into :some-table)
               (h/columns :col1 :col2)
               (h/values [[1 2] [3 4]])
               sql/format)))
    (is (= ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
           (-> (mysql-h/insert-ignore-into :some-table)
               (h/values [{:col1 1 :col2 2} {:col1 3 :col2 4}])
               sql/format)))))

(comment
  (run-tests))




