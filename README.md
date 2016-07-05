[![Build Status](https://travis-ci.org/theAgileFactory/maf-desktop-app.svg?branch=master)](https://travis-ci.org/theAgileFactory/maf-desktop-app)

# Desktop App
The Desktop App component is the functional part of the BizDock software. It contains among others the controllers, the DAO and the views.

Find <a href="https://help.bizdock.io/doku.php">here</a> the full description of the BizDock software.


## Development environment
This repo contains a sub-folder name ```development```.
This one contains some scripts and resources for who wants to develop BizDock.

### Copyright
This folder contains a script tp be used to add the GPL license into the Java and scala templates.

### Tools
The tools folder contains whatever is required to code BizDock software.
There are two sub-folders:
* ```environment``` : contains the folders which contains the BizDock data
  * ```deadletters``` : is used by Akka (see Akka documentation). In BizDock Akka is used for many asynchronous processes.
  * ```deadletters-reprocessing``` : contains, in case of system stop, the messages which have not been processed.
  These ones are supposed to be reprocessed at startup.
  * ```maf-filesystem``` : is the folder structure which contains various artifacts required by BizDock (extensions, file attachments, SAMLv2 configuration, etc.) 
* ```sample-data``` : contains some sample test data to be loaded in your development instance (and modified when new features are added). The users created by this sample data set have the password "pass1234".

The tools folder contains also some scripts and a configuration file:
* ```env.cfg``` : the development environment configuration file.
Before using the scripts one should set the ```BIZDOCK_GIT_ROOT``` property with the right value (basically the path to the folder which contains the various repos of BizDock.
* ```full_build.sh``` : rebuild the application (please see ```-h``` modifier for the various options
* ```install_extensions.sh``` : install the default BizDock extensions (NB: if you are modifying the default extensions please ensure that the ```maf-deafultplugins-extension``` project has been previously built otherwise the maven central published version will be used).
* ```restart_database.sh``` : start or restart the BizDock database container (a MariaDB container) which is the same one as the one defined by the [bizdock-installation](https://github.com/theAgileFactory/bizdock-installation) project.
This container exposes the standard MySQL port 3306 and listen on the loopback (127.0.0.1).
* ```stop_database.sh``` : stop the database container
* ```db_init.sh``` : load the database with some data (it drops the current database and creates a new one)

