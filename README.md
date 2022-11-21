# honeysql-mysql
MySQL extensions for [honeysql](https://github.com/seancorfield/honeysql)

## Usage
### REPL
```clj
(require '[honey.sql :as sql]
         '[honey.sql.helpers :as h]
         '[honey.sql.my.helpers :as mh])
```

### insert-ignore-into
```clojure
(-> (mh/insert-ignore-into :some-table)
    (h/columns :col1 :col2)
    (h/values [[1 2] [3 4]])
    sql/format)
;; => ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
```

### fulltext search
```clojure
(-> (h/select :*)
    (h/from :foo)
    (h/where [:match-against [:contents] "word1 word2"])
    (sql/format {:inline true}))
;; => ["SELECT * FROM foo WHERE MATCH (contents) AGAINST ('word1 word2')"]

(-> (h/select :*)
    (h/from :foo)
    (h/where [:match-against [:contents] "word1 word2" :in-boolean-mode])
    (sql/format {:inline true}))
;; => ["SELECT * FROM foo WHERE MATCH (contents) AGAINST ('word1 word2' IN BOOLEAN MODE)"]

(-> (h/select :id :body [[:match-against [:title :body] "some text" :in-natural-language-mode] :score])
    (h/from :articles)
    (h/where [:match-against [:title :body] "some text" :in-natural-language-mode])
    (sql/format {:inline true}))
;; => ["SELECT id, body, MATCH (title, body) AGAINST ('some text' IN NATURAL LANGUAGE MODE) AS score FROM articles WHERE MATCH (title, body) AGAINST ('some text' IN NATURAL LANGUAGE MODE)"]
```

### explain
```clojure
(-> (mh/explain)
    (h/select :*)
    (h/from :foo)
    (h/where [:= :col1 1])
    sql/format)
;; => ["EXPLAIN SELECT * FROM foo WHERE col1 = ?" 1]

;; Use format
;; Available formats: :traditional, :tree, :json
(-> (mh/explain :tree)
    (h/select :*)
    (h/from :foo)
    (h/where [:= :col1 1])
    sql/format)
;; => ["EXPLAIN FORMAT=TREE SELECT * FROM foo WHERE col1 = ?" 1]
```

### optimizer hints
Only supports index level hints with select yet

```clojure
(-> (mh/select-with-optimizer-hints [:*] [[:index-merge :t1 [:i-a :i-b :i-c]]])
    (h/from :t1)
    (h/where [:and [:= :a 1] [:= :b 2]])
    (sql/format {:inline true}))
;; => ["SELECT /*+ INDEX_MERGE(t1 i_a, i_b, i_c) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]

;; You can use multiple hints at once
(-> (mh/select-with-optimizer-hints [:*] [[:no-index-merge :t1 [:i-a :i-b]]
                                          [:index-merge :t1 [:i-b]]])
    (h/from :t1)
    (h/where [:and [:= :a 1] [:= :b 2]])
    (sql/format {:inline true}))
;; => ["SELECT /*+ NO_INDEX_MERGE(t1 i_a, i_b) INDEX_MERGE(t1 i_b) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]
```

### values with alias
```clojure
(-> (h/insert-into :foo)
    (mh/values-as [{:a 1 :b 2} {:a 3 :b 4}] :new)
    (h/on-duplicate-key-update {:b :new.b})
    sql/format)
;; => ["INSERT INTO foo (a, b) VALUES (?, ?), (?, ?) AS new ON DUPLICATE KEY UPDATE b = new.b" 1 2 3 4]
```

### timestampdiff function
```clojure
(-> (h/select :*)
    (h/from :table)
    (h/where :< [:timestampdiff :hour :expired_at [:now]] 24)
    (sql/format {:dialect :mysql
                 :quoted true}))
;; => ["SELECT * FROM `table` WHERE TIMESTAMPDIFF(HOUR, `expired_at`, NOW()) < ?" 24]
```

### select straight join
```clojure
(-> (mh/select-straight-join :*
                             :col
                             [:col :col-alias]
                             [[:now] :now])
    (h/from :table)
    (sql/format {:dialect :mysql
                 :quoted true
                 :quoted-snake true}))
;; => ["SELECT STRAIGHT_JOIN *, `col`, `col` AS `col_alias`, NOW() AS `now` FROM `table`"]
```

### group concat
```clojure
(-> (h/select [[:group-concat :col1]])
    (h/from :table1)
    (sql/format {:dialect      :mysql
                 :quoted       true
                 :quoted-snake true}))
;; => ["SELECT GROUP_CONCAT(`col1`) FROM `table1`"]

(-> (h/select [[:group-concat [:distinct :col1]] :c1])
    (h/from :table1)
    (sql/format {:dialect      :mysql
                 :quoted       true
                 :quoted-snake true}))
;; => ["SELECT GROUP_CONCAT(DISTINCT `col1`) AS `c1` FROM `table1`"]

(-> (h/select [[:group-concat [:distinct :col1] {:order-by [[:col2 :desc]] :seperator "|"}]])
    (h/from :table1)
    (sql/format {:dialect      :mysql
                 :quoted       true
                 :quoted-snake true}))
;; => ["SELECT GROUP_CONCAT(DISTINCT `col1` ORDER BY `col2` DESC SEPARATOR '|') FROM `table1`"]
```

## Run tests
```bash
clj -X:test
```
