(ns honeysql-mysql.mysql-test
  (:require [clojure.test :refer [deftest run-tests is testing]]
            [honeysql-mysql.format :refer [extend-syntax!]]
            [honeysql-mysql.helpers :as mysql-h]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(clojure.test/use-fixtures :once (fn [f]
                                   (extend-syntax!)
                                   (f)))

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

(deftest match-against-test
  (testing "search-mode"
    (is (= ["WHERE MATCH (contents) AGAINST ('검색어1 검색어2')"]
           (sql/format
             {:where  [:match-against [:contents] "검색어1 검색어2"]}
             {:inline true})))
    (is (= ["WHERE MATCH (contents) AGAINST ('검색어1 검색어2' IN BOOLEAN MODE)"]
           (sql/format
             {:where  [:match-against [:contents] "검색어1 검색어2" :in-boolean-mode]}
             {:inline true}))))

  (testing "in where"
    (let [orderer-name "john"]
      (is (= ["WHERE MATCH (orderer_name) AGAINST (? IN BOOLEAN MODE)" "john"]
             (sql/format {:where [:match-against [:orderer_name] orderer-name :in-boolean-mode]})))))

  (testing "in select"
    (let [text "Security implications of running MySQL as root"
          mode :in-natural-language-mode]
      (is (= ["SELECT id, body, MATCH (title, body) AGAINST ('Security implications of running MySQL as root' IN NATURAL LANGUAGE MODE) AS score FROM articles WHERE (MATCH (title, body) AGAINST ('Security implications of running MySQL as root' IN NATURAL LANGUAGE MODE))"])
          (sql/format {:select [:id :body [[:match-against [:title :body] text mode] :score]]
                       :from   :articles
                       :where  [:match-against [:title :body] text mode]}
                      {:inline true})))))

(comment
  (run-tests))
