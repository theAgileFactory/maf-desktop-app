#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

#This script performs a full rebuild of the various components of the application (framework, datamodel, etc.)
#You must SVN update these components before triggering the full rebuild

OPTS=`getopt -a -o h -l help -o f -l framework -o m -l model -o d -l desktop -- "$0" "$@"`
HELP=$'Possible arguments : \n\t--help (-h)\n\t--framework (-f)\n\t--model (-m)\n\t--desktop (-d)\nOnly one option can be used because the --framework option will compile everything while --model will only compile model and desktop'

# Functions definition 

Framework () {
  echo "---- BUILDING FRAMEWORK ----"
  mvn -f $BIZDOCK_GIT_ROOT$APP_FRAMEWORK_PROJECT/pom.xml -Dgpg.skip clean install
  STATUS=$?
  if [ $STATUS -ne 0 ]; then
    exit
  fi
  if [ "$USE_ECLIPSE" = "true" ]; then
  	echo ">>> Configuring the eclipse project"
    mvn -f $BIZDOCK_GIT_ROOT$APP_FRAMEWORK_PROJECT/pom.xml play2:eclipse
    if [ $STATUS -ne 0 ]; then
      exit
    fi
  fi
  Model
}

Model () {
  echo "---- BUILDING DATA MODEL ----"
  mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_DATAMODEL/pom.xml -Dgpg.skip clean install
  STATUS=$?
  if [ $STATUS -ne 0 ]; then
    exit
  fi
  if [ "$USE_ECLIPSE" = "true" ]; then
    echo ">>> Configuring the eclipse project"
    mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_DATAMODEL/pom.xml play2:eclipse
    if [ $STATUS -ne 0 ]; then
      exit
    fi
  fi
  sleep 5
  Desktop
}

Desktop () {
  echo "---- BUILDING DESKTOP ----"
  mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml -Dgpg.skip clean install
  STATUS=$?
  if [ $STATUS -ne 0 ]; then
    exit
  fi
  if [ "$USE_ECLIPSE" = "true" ]; then
    echo ">>> Configuring the eclipse project"
    mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml play2:eclipse
    if [ $STATUS -ne 0 ]; then
      exit
    fi
  fi
}

End () {
  if [ -n "$1" ];then
    echo "$1 is not a valid argument. See -h for help.";
  fi
}

if [ "$#" -gt 1 ]; then
  echo "$HELP"
  exit 1
fi

if [ "$#" -eq 0 ]; then
  Framework
  exit 0
fi

if [ $? != 0 ] # There was an error parsing the options
then
  echo "Unkown option $1"
  exit 1 
fi

eval set -- "$OPTS"

# Process the arguments
while true; do
  case "$1" in
    --help) echo "$HELP"; shift;;
    -h) echo "$HELP"; shift;;
    --framework) Framework; #Call the Framework function
      shift;;
    -f) Framework; #Call the Framework function
      shift;;
    --model) Model; #Call the Model function
      shift;;
    -m) Model; #Call the Model function
      shift;;
    --desktop) Desktop; #Call the Desktop function
      shift;;
    -d) Desktop; #Call the Desktop function
      shift;;
    --) End $3; #Call the End function
      shift; break;;
  esac
done
