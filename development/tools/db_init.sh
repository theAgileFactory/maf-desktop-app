#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPT_DIR/env.cfg

#This script dump and recreate the database of the desktop.

echo "---- REFRESH DBML ----"

echo -e "\n>>> Prepare the dbmdl-framework package..."
rm -rf $SCRIPT_DIR/dbmdl-framework
cp -R $BIZDOCK_GIT_ROOT$DBMDL_FRAMEWORK_PROJECT $SCRIPT_DIR/dbmdl-framework
cd dbmdl-framework
mvn -f pom2.xml clean package
cd target
packageVersion=$(cat version.properties)
mvn com.agifac.deploy:replacer-maven-plugin:replace -Dsource=dbmdl-framework-$packageVersion.zip -Denv=properties/empty.properties
unzip -d script merged-dbmdl-framework-$packageVersion.zip
cd script/scripts
chmod u+x *.sh
cd $SCRIPT_DIR
echo -e "\n...done !\n\n"

echo -e "\n>>> Prepare the maf-dbmdl package"
rm -rf $SCRIPT_DIR/maf-dbmdl
cp -R $BIZDOCK_GIT_ROOT$MAF_DBMDL_PROJECT $SCRIPT_DIR/maf-dbmdl
cd maf-dbmdl
mvn -f pom2.xml clean package
cd target
packageVersion=$(cat version.properties)
mvn com.agifac.deploy:replacer-maven-plugin:replace -Dsource=maf-dbmdl-$packageVersion.zip -Denv=properties/empty.properties
unzip -d script merged-maf-dbmdl-$packageVersion.zip
cd script/scripts
chmod u+x *.sh
cd $SCRIPT_DIR
echo -e "\n...done !\n\n"

echo "------- DROP SCHEMA -------"
echo -e "\n>>> Clear the database..."
mysql -h 127.0.0.1 -u maf -pmaf < $SCRIPT_DIR/sample-data/reset.sql
echo -e "\n...done !\n\n"

echo "------- CREATE SCHEMA -------"
echo -e "\n>>> Run the dbmdl-framework..."
chmod u+x $SCRIPT_DIR/dbmdl-framework/target/script/scripts/run.sh
cd $SCRIPT_DIR/dbmdl-framework/target/script/scripts
./run.sh
echo -e "\n...done !\n\n"

echo -e "\n>>> run the maf-dbmdl..."
chmod u+x $SCRIPT_DIR/dbmdl-framework/target/script/scripts/run.sh
cd $SCRIPT_DIR/maf-dbmdl/target/script/scripts
./run.sh
echo -e "\n...done !\n\n"

echo "------- LOAD INITIALIZATION DATA ------"
echo -e "\n>>> Load basic data..."
mysql -h 127.0.0.1 -u maf -pmaf maf < $BIZDOCK_GIT_ROOT$MAF_DESKTOP_PROJECT/conf/sql/init_base.sql
echo -e "\n...done !\n\n"

echo -e "\n>>> Load sample data..."
mysql -h 127.0.0.1 -u maf -pmaf maf < $SCRIPT_DIR/sample-data/init_data.sql
echo -e "\n...done !\n\n"

