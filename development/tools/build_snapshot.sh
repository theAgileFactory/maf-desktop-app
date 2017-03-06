#!/bin/sh

echo ">>> Perform BizDock full build"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
${SCRIPT_DIR}/full_build.sh
STATUS=$?
if [ $STATUS -ne 0 ]; then
    exit
fi

echo ">>> Build packaging"
mvn -f ${BIZDOCK_GIT_ROOT}${BIZDOCK_PACKAGING_PROJECT}/pom2.xml -Dgpg.skip clean install
STATUS=$?
if [ $STATUS -ne 0 ]; then
    exit
fi

echo ">>> Build docker image"
${BIZDOCK_GIT_ROOT}${BIZDOCK_INSTALLATION_PROJECT}/build_image.sh
STATUS=$?
if [ $STATUS -ne 0 ]; then
    exit
fi

echo ">>> Pushing to docker repository"
docker push bizdock/bizdock:$(cat ${BIZDOCK_GIT_ROOT}${BIZDOCK_INSTALLATION_PROJECT}/target/version.properties)
