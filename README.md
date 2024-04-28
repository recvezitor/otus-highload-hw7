# hw7 Шардирование

* монолит
* реактивщина
* без ОРМ
* добавлены индексы к полям для поиска
* добавлено кеширование ленты друзей
* добавлены вебсокеты и реббит
* добавлены диалоги + шардирование


### Инструкция
Диалог - это разговор 2х людей, уникально идентифицируется id 2х разговаривающих пользователей, не зависимо от порядка.
В  этом ДЗ как я понял нет необходимости разделять монолит на 2 микросервиса, акцент на шардировании и правильном выборе ключа шардирования.
В качестве ключа шардирования будем использовать dialogId, который формируется как order independent хеш от userId1 и userId2. (md5HashInvariant(user1, user2))
В этом случае отсутсвует риск поиска по нескольким шардам в отличие от случая если бы мы использовали в качестве ключа userId1 or userId2
Но если нам понадобится найти все диалоги отдельного пользователя, то для него лучше создать дополнительную таблицу со связью user->все диалоги где он учавствует
Из недостатоков выбора dialogId в качестве ключа шардирования - придется его включить в primary key, citus по другому не умеет
Решардинг производится средствами citus: SELECT citus_rebalance_start();

```SQL
explain select * from otus_highload.dialog_message where dialog_id='e4b1295294d075f88f5fa52a65a1e1ed';
```
```bash
Custom Scan (Citus Adaptive)  (cost=0.00..0.00 rows=0 width=0)
  Task Count: 1
  Tasks Shown: All
  ->  Task
        Node: host=localhost port=5432 dbname=postgres
        ->  Seq Scan on dialog_message_102014 dialog_message  (cost=0.00..16.50 rows=3 width=128)
        Filter: ((dialog_id)::text = 'e4b1295294d075f88f5fa52a65a1e1ed'::text)
```


### install

mvn clean install
docker build -f docker/Dockerfile.jvm -t otus-highload-hw7:latest .
docker images

### publish

docker tag otus-highload-hw7:latest recvezitor/otus-highload-hw7:latest
docker login -> recvezitor/password
docker push recvezitor/otus-highload-hw7:latest

### deploy 

docker compose -f ./docker/docker-compose.yml -p citus up --scale worker=2 -d
docker compose -p citus up --scale worker=2 -d


### hits

explain select * from otus_highload.dialog_message where dialog_id='e4b1295294d075f88f5fa52a65a1e1ed';
SELECT shard_count FROM citus_tables WHERE table_name::text = 'otus_highload.dialog_message';
SELECT nodename, count(*) FROM citus_shards GROUP BY nodename;
SELECT master_get_active_worker_nodes();

https://github.com/a-poliakov/highload-arch-examples/tree/main/sharding/postgres