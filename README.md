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

## Run tests
```bash
clj -X:test
```
