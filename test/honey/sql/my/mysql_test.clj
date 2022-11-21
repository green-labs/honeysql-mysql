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
               sql/format)))
    (is (= ["INSERT IGNORE INTO `some_table` (`col1`, `col2`) VALUES (?, ?), (?, ?)" 1 2 3 4]
           (-> (mh/insert-ignore-into :some-table)
               (h/values [{:col1 1 :col2 2} {:col1 3 :col2 4}])
               (sql/format {:dialect :mysql
                            :quoted true
                            :quoted-snake true}))))))

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
           (-> (mh/select-with-optimizer-hints [:*] [[:index-merge :t1 [:i-a :i-b :i-c]]])
               (h/from :t1)
               (h/where [:and [:= :a 1] [:= :b 2]])
               (sql/format {:inline true}))))
    (is (= ["SELECT /*+ NO_ORDER_INDEX(`t1` `i_a`, `i_b`, `i_c`) */ `col_a`, `col_b`, `col_c` FROM `t1`"]
           (-> (mh/select-with-optimizer-hints [:col-a :col-b :col-c] [[:no-order-index :t1 [:i-a :i-b :i-c]]])
               (h/from :t1)
               (sql/format {:dialect :mysql
                            :quoted-snake true})))))
  (testing "Use multiple hints"
    (is (= ["SELECT /*+ NO_INDEX_MERGE(t1 i_a, i_b) INDEX_MERGE(t1 i_b) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]
           (-> (mh/select-with-optimizer-hints [:*] [[:no-index-merge :t1 [:i-a :i-b]]
                                                     [:index-merge :t1 [:i-b]]])
               (h/from :t1)
               (h/where [:and [:= :a 1] [:= :b 2]])
               (sql/format {:inline true})))))
  (testing "JOIN_PREFIX() hint test"
    (is (= ["SELECT /*+ JOIN_PREFIX(t1, t2) */ * FROM table1 AS t1 LEFT JOIN table2 AS t2 ON t1 = t2 WHERE t1.role = ?" "admin"]
           (-> (mh/select-with-optimizer-hints [:*] [[:join-prefix [:t1 :t2]]])
               (h/from [:table1 :t1])
               (h/left-join [:table2 :t2] [:= :t1 :t2])
               (h/where [:= :t1.role "admin"])
               sql/format))))
  (testing "join level hint X index levelt hint"
    (is (= ["SELECT /*+ JOIN_PREFIX(t1, t2) INDEX_MERGE(t1 i_a, i_b, i_c) */ * FROM table1 AS t1 LEFT JOIN table2 AS t2 ON t1 = t2 WHERE t1.role = ?" "admin"]
         (-> (mh/select-with-optimizer-hints [:*] [[:join-prefix [:t1 :t2]]
                                                     [:index-merge :t1 [:i-a :i-b :i-c]]])
               (h/from [:table1 :t1])
               (h/left-join [:table2 :t2] [:= :t1 :t2])
               (h/where [:= :t1.role "admin"])
               sql/format))))
  (testing "remove unsupported hint test"
    (is (= (-> (mh/select-with-optimizer-hints [:*] [[:join-prefix [:t1 :t2]]
                                                     [:index-merge :t1 [:i-a :i-b :i-c]]
                                                     [:unsupport-merge :a [:i-a :i-b :i-c]]])
               (h/from [:table1 :t1])
               (h/left-join [:table2 :t2] [:= :t1 :t2])
               (h/where [:= :t1.role "admin"])
               sql/format)))))

(deftest values-as-test
  (testing "use row alias"
    (is (= ["INSERT INTO foo (a, b) VALUES (?, ?), (?, ?) AS new ON DUPLICATE KEY UPDATE b = new.b" 1 2 3 4]
           (-> (h/insert-into :foo)
               (mh/values-as [{:a 1 :b 2} {:a 3 :b 4}] :new)
               (h/on-duplicate-key-update {:b :new.b})
               sql/format)))))

(deftest timestampdiff-test
  (is (= ["SELECT * FROM `table` WHERE TIMESTAMPDIFF(HOUR, `expired_at`, NOW()) < ?" 24]
         (-> (h/select :*)
             (h/from :table)
             (h/where :< [:timestampdiff :hour :expired_at [:now]] 24)
             (sql/format {:dialect :mysql
                          :quoted true}))))
  (is (= ["SELECT TIMESTAMPDIFF(YEAR, `birth_date`, NOW()) AS `age` FROM `user`"]
         (-> (h/select [[:timestampdiff :year :birth_date [:now]] :age])
             (h/from :user)
             (sql/format {:dialect :mysql
                          :quoted true})))))

(deftest select-straight-join-test
  (is (= ["SELECT STRAIGHT_JOIN *, `col`, `col` AS `col_alias`, NOW() AS `now` FROM `table`"]
         (-> (mh/select-straight-join :*
                                      :col
                                      [:col :col-alias]
                                      [[:now] :now])
             (h/from :table)
             (sql/format {:dialect :mysql
                          :quoted true
                          :quoted-snake true})))))

(deftest group-concat-test
  (testing "Simple GROUP_CONCAT"
    (is (= ["SELECT GROUP_CONCAT(`col1`) FROM `table1`"]
           (-> (h/select [[:group-concat :col1]])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true})))))
  (testing "GROUP_CONCAT with DISTINCT"
    (is (= ["SELECT GROUP_CONCAT(DISTINCT `col1`) FROM `table1`"]
           (-> (h/select [[:group-concat [:distinct :col1]]])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true})))))
  (testing "GROUP_CONCAT with column alias"
    (is (= ["SELECT GROUP_CONCAT(DISTINCT `col1`) AS `c1` FROM `table1`"]
           (-> (h/select [[:group-concat [:distinct :col1]] :c1])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true})))))
  (testing "GROUP_CONCAT with SEPARATOR"
    (is (= ["SELECT GROUP_CONCAT(DISTINCT `col1` SEPARATOR '|') FROM `table1`"]
           (-> (h/select [[:group-concat [:distinct :col1] {:separator "|"}]])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true})))))
  (testing "GROUP_CONCAT with ORDER BY"
    (is (= ["SELECT GROUP_CONCAT(`col1` ORDER BY `col2` DESC) FROM `table1`"]
           (-> (h/select [[:group-concat :col1 {:order-by [[:col2 :desc]]}]])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true})))))
  (testing "GROUP_CONCAT with DISTINCT, ORDER BY, SEPARATOR"
    (is (= ["SELECT GROUP_CONCAT(DISTINCT `col1` ORDER BY `col2` DESC SEPARATOR '|') FROM `table1`"]
           (-> (h/select [[:group-concat :col1 {:order-by  [[:col2 :desc]]
                                                :separator "|"}]])
               (h/from :table1)
               (sql/format {:dialect      :mysql
                            :quoted       true
                            :quoted-snake true}))))))

(comment
  (run-tests))
