#!/bin/bash

HELP=$'Available options: \n\t-b - GitHub branch to be requested\n\t-p - Project repository to be requested\n\t-h - help\nExample: travis_request.sh -b master -p maf-desktop-app'

if [ $? != 0 ] # There was an error parsing the options
then
  echo "Unkown option $1"
  echo "$HELP"
  exit 1
fi

while getopts "b:p:h" option
do
  case $option in
    b)
      BRANCH_NAME="$OPTARG"
      ;;
    p)
      PROJECT_NAME="$OPTARG"
      ;;
    h)
      echo "$HELP"
      exit 0
      ;;
    :)
      echo "Option -$OPTARG needs an argument"
      exit 1
      ;;
    \?)
      echo "$OPTARG : invalid option"
      exit 1
      ;;
  esac
done

echo "Requesting travis build for project $PROJECT_NAME on branch $BRANCH_NAME"

body="{
\"request\": {
  \"branch\":\"$BRANCH_NAME\"
}}"

curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token $TTOKEN" \
  -d "$body" \
  https://api.travis-ci.org/repo/theAgileFactory%2F${PROJECT_NAME}/requests
