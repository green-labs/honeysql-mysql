(ns honey.sql.my.mysql-test
  (:require [clojure.test :refer [deftest run-tests is testing]]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [honey.sql.my.format]
            [honey.sql.my.helpers :as mh]))

(deftest insert-ignore-into-test
  (testing "INSERT IGNORE INTO sql generation for mysql"
    (is (= ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
           (-> (mh/insert-ignore-into :some-table)
               (h/columns :col1 :col2)
               (h/values [[1 2] [3 4]])
               sql/format)))
    (is (= ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
           (-> (mh/insert-ignore-into :some-table)
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

(deftest explain-test 
  (testing "without format"
    (is (= ["EXPLAIN SELECT * FROM foo WHERE col1 = ?" 1]
           (-> (mh/explain)
               (h/select :*)
               (h/from :foo)
               (h/where [:= :col1 1])
               sql/format))))
  (testing "traditional format"
    (is (= ["EXPLAIN FORMAT=TRADITIONAL SELECT * FROM foo WHERE col1 = ?" 1]
           (-> (mh/explain :traditional)
               (h/select :*)
               (h/from :foo)
               (h/where [:= :col1 1])
               sql/format))))
  (testing "json format"
    (is (= ["EXPLAIN FORMAT=JSON SELECT * FROM foo WHERE col1 = ?" 1]
           (-> (mh/explain :json)
               (h/select :*)
               (h/from :foo)
               (h/where [:= :col1 1])
               sql/format))))
  (testing "tree format"
    (is (= ["EXPLAIN FORMAT=TREE SELECT * FROM foo WHERE col1 = ?" 1]
           (-> (mh/explain :tree)
               (h/select :*)
               (h/from :foo)
               (h/where [:= :col1 1])
               sql/format)))))

(deftest select-with-optimizer-hints-test
  (testing "Use only one hint"
    (is (= ["SELECT /*+ INDEX_MERGE(t1 i_a, i_b, i_c) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]
           (-> (mh/select-with-optimizer-hints [:*] [[:index-merge "t1" ["i_a" "i_b" "i_c"]]])
               (h/from :t1)
               (h/where [:and [:= :a 1] [:= :b 2]])
               (sql/format {:inline true})))))
  (testing "Use multiple hints"
    (is (= ["SELECT /*+ NO_INDEX_MERGE(t1 i_a, i_b) INDEX_MERGE(t1 i_b) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]
           (-> (mh/select-with-optimizer-hints [:*] [[:no-index-merge "t1" ["i_a" "i_b"]]
                                                     [:index-merge "t1" ["i_b"]]])
               (h/from :t1)
               (h/where [:and [:= :a 1] [:= :b 2]])
               (sql/format {:inline true}))))))

(comment
  (run-tests))
