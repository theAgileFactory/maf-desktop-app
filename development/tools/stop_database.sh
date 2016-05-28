#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

echo "---- STOPPING DATABASE ----"
docker stop ${DB_CONTAINER_NAME}
docker rm ${DB_CONTAINER_NAME}