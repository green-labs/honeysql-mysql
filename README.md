# honeysql-mysql
MySQL extensions for [honeysql](https://github.com/seancorfield/honeysql)

## Usage
### REPL
```clj
(require '[honeysql.core :as sql]
         '[honeysql.helpers :as h]
         '[honeysql-mysql.helpers :as mysqlh])
```

### insert-ignore-into
```clojure
(sql/format {:insert-ignore-into :some-table
             :columns            [:col1 :col2]
             :values             [[1 2] [3 4]]})
(sql/format (-> (mysqlh/insert-ignore-into :some-table)
                (h/columns :col1 :col2)
                (h/values [[1 2] [3 4]])))
;; => ["INSERT IGNORE INTO some_table (col1, col2) VALUES (?, ?), (?, ?)" 1 2 3 4]
```
