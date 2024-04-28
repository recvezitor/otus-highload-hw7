# hw7 

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


https://github.com/a-poliakov/highload-arch-examples/tree/main/sharding/postgres