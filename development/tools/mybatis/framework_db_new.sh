#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

cd $BIZDOCK_GIT_ROOT$DBMDL_FRAMEWORK_PROJECT/src/main/resources/scripts/
./migrate.sh new $1 --path=../repo --env=development

git add $BIZDOCK_GIT_ROOT$DBMDL_FRAMEWORK_PROJECT/src/main/resources/repo/scripts/*.sql