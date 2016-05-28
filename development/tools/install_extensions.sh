#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.agifac.lib:maf-defaultplugins-extension:LATEST:jar
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=com.agifac.lib:maf-defaultplugins-extension:LATEST:jar -DoutputDirectory=$SCRIPT_DIR
rm $SCRIPT_DIR/environment/maf-filesystem/extensions/*.jar
mv $SCRIPT_DIR/maf-defaultplugins-extension-*.jar $SCRIPT_DIR/environment/maf-filesystem/extensions
