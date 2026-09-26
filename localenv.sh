#!/bin/bash

echo '### Stop and remove containers ###'
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

echo '### Run mysql db ###'
docker run --name rfr-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -v mysql-volume:/var/lib/mysql -d mysql:8.4.7
