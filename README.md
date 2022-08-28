# honeysql-mysql
MySQL extensions for [honeysql](https://github.com/seancorfield/honeysql)

## Usage
### REPL
```clj
(require '[honey.sql.core :as sql]
         '[honey.sql.helpers :as h]
         '[honeysql-mysql.helpers :as mysql-h])
```

### insert-ignore-into
```clojure
(-> (mysql-h/insert-ignore-into :some-table)
    (h/columns :col1 :col2)
    (h/values [[1 2] [3 4]])
    sql/format)
;; => ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
```

## Run tests
```bash
clj -X:test
```
