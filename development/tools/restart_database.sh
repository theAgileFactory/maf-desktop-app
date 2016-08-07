#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

echo "---- DATABASE VOLUME CREATION (if it does not exists) ----"
docker volume create --name=${DB_VOLUME_NAME}

echo "---- RESTART THE DATABASE CONTAINER ----"
INSTANCE_TEST=$(docker ps | grep -e "development_bizdockdb$")
if [ $? -eq 0 ]; then
  echo ">>> Stopping the database container ${DB_CONTAINER_NAME}..."
  docker stop ${DB_CONTAINER_NAME}
  echo "... ${DB_CONTAINER_NAME} is stopped !"
fi
  
INSTANCE_TEST=$(docker ps -a | grep -e "${DB_CONTAINER_NAME}$")
if [ $? -eq 0 ]; then
  echo ">>> Deleting the stopped database container ${DB_CONTAINER_NAME}..."
  docker rm ${DB_CONTAINER_NAME}
  echo "... ${DB_CONTAINER_NAME} is deleted !"
fi

echo "---- RUNNING DATABASE CONTAINER ----"
echo ">> Starting the database container ${DB_CONTAINER_NAME} ..."
docker run --name=${DB_CONTAINER_NAME} -d \
  -p ${DB_PORT}:3306 \
  -v ${DB_VOLUME_NAME}:/var/lib/mysql/ \
  -v $SCRIPT_DIR/database_dumps:/var/opt/db/dumps/ \
  -v $SCRIPT_DIR/database_dumps:/var/opt/db/cron/ \
  -e MYSQL_ROOT_PASSWORD="$DB_ROOT_PASSWD" \
  -e MYSQL_USER="$DB_USER" \
  -e MYSQL_PASSWORD="$DB_USER_PASSWD" \
  -e MYSQL_DATABASE="$DB_NAME" \
  bizdock/bizdock_mariadb:10.1.12 --useruid $(id -u $(whoami)) --username $(whoami)
echo "... start command completed"

#test if db container is up
if [ -z "$(docker ps | grep ${DB_CONTAINER_NAME}$)" ]; then
  echo "/!\\ Database container is not up. Stopping /!\\"
  exit 1
fi