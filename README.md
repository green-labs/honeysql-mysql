# honeysql-mysql
MySQL extensions for [honeysql](https://github.com/seancorfield/honeysql)

## Usage
### REPL
```clj
(require '[honey.sql.core :as sql]
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
(-> (mh/select-with-optimizer-hints [:*] [[:index-merge "t1" ["i_a" "i_b" "i_c"]]])
    (h/from :t1)
    (h/where [:and [:= :a 1] [:= :b 2]])
    (sql/format {:inline true}))
;; => ["SELECT /*+ INDEX_MERGE(t1 i_a, i_b, i_c) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]

;; You can use multiple hints at once
(-> (mh/select-with-optimizer-hints [:*] [[:no-index-merge "t1" ["i_a" "i_b"]]
                                          [:index-merge "t1" ["i_b"]]])
    (h/from :t1)
    (h/where [:and [:= :a 1] [:= :b 2]])
    (sql/format {:inline true}))
;; => ["SELECT /*+ NO_INDEX_MERGE(t1 i_a, i_b) INDEX_MERGE(t1 i_b) */ * FROM t1 WHERE (a = 1) AND (b = 2)"]
```

## Run tests
```bash
clj -X:test
```
