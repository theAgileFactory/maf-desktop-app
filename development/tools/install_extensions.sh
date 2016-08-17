#!/bin/sh

#This script expect as a parameter the version of the default plugin to download
if [ -z "$1" ]
  then
	echo "Please proide the version of the artifact to be downloaded as a parameter !"
 	exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.agifac.lib:maf-defaultplugins-extension:$1:jar
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -DoverWriteIfNewer=true -Dartifact=com.agifac.lib:maf-defaultplugins-extension:$1:jar -DoutputDirectory=$SCRIPT_DIR
rm $SCRIPT_DIR/environment/maf-filesystem/extensions/*.jar
mv $SCRIPT_DIR/maf-defaultplugins-extension-*.jar $SCRIPT_DIR/environment/maf-filesystem/extensions
