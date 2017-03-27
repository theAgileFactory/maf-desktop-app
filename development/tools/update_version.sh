#!/bin/sh

if [ -z "$1" ]
  then
	echo "Please provide the new version of BizDock as a parameter !"
 	exit 1
fi

NEW_VERSION=$1

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

echo "Building app-framework with new version"
mvn -f $BIZDOCK_GIT_ROOT$APP_FRAMEWORK_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$APP_FRAMEWORK_PROJECT/pom.xml install -Dgpg.skip

echo "Building maf-desktop-datamodel with new version"
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_DATAMODEL/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_DATAMODEL/pom.xml versions:update-property -Dproperty="lib.app-framework.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_DATAMODEL/pom.xml install -Dgpg.skip

echo "Building maf-desktop-app with new version"
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml versions:update-property -Dproperty="lib.app-framework.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml versions:update-property -Dproperty="maf.maf-desktop-datamodel.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/pom.xml install -Dgpg.skip

echo "Building maf-defaultplugins-extension with new version"
mvn -f $BIZDOCK_GIT_ROOT$MAF_DEFAULTPLUGINS_EXTENSION/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$MAF_DEFAULTPLUGINS_EXTENSION/pom.xml versions:update-property -Dproperty="lib.app-framework.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DEFAULTPLUGINS_EXTENSION/pom.xml versions:update-property -Dproperty="maf.maf-desktop-datamodel.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DEFAULTPLUGINS_EXTENSION/pom.xml versions:update-property -Dproperty="maf-desktop-app.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$MAF_DEFAULTPLUGINS_EXTENSION/pom.xml install -Dgpg.skip

echo "Building bizdock-packaging with new version"
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_PACKAGING_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_PACKAGING_PROJECT/pom.xml versions:update-property -Dproperty="play.app.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_PACKAGING_PROJECT/pom.xml versions:update-property -Dproperty="maf-defaultplugins-extension.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_PACKAGING_PROJECT/pom.xml install -Dgpg.skip

echo "Building maf-dbmdl with new version"
mvn -f $BIZDOCK_GIT_ROOT$MAF_DBMDL_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$MAF_DBMDL_PROJECT/pom.xml install -Dgpg.skip

echo "Building dbmdl-framework with new version"
mvn -f $BIZDOCK_GIT_ROOT$DBMDL_FRAMEWORK_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$DBMDL_FRAMEWORK_PROJECT/pom.xml install -Dgpg.skip

echo "Building bizdock-installation with new version"
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml versions:update-property -Dproperty="play.app.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml versions:update-property -Dproperty="maf-defaultplugins-extension.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml versions:update-property -Dproperty="maf.dbmdl.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml versions:update-property -Dproperty="dbmdl.framework.version" -DnewVersion="[$NEW_VERSION]" -DallowSnapshots=true
mvn -f $BIZDOCK_GIT_ROOT$BIZDOCK_INSTALLATION_PROJECT/pom.xml install -Dgpg.skip

echo "All modules version changed to $NEW_VERSION"