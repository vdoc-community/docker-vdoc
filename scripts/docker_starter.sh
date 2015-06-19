#!/bin/bash

# create modules.properties from env
rm $VDOC_HOME/configurator/modules.properties
modulesFileName="$VDOC_HOME/configurator/modules.properties"
echo module.easysite.active=$MODULE_EASYSITE >> $modulesFileName
echo module.vdocopen.active=0 >> $modulesFileName
echo module.vdpdomino.active=0 >> $modulesFileName
echo module.editorials.active=$MODULE_EDITORIALS >> $modulesFileName
echo module.links.active=$MODULE_LINKS >> $modulesFileName
echo module.forums.active=$MODULE_FORUMS >> $modulesFileName
echo module.news2.active=$MODULE_NEWS >> $modulesFileName
echo module.polls.active=$MODULE_POLLS >> $modulesFileName
echo module.reportImport.active=0 >> $modulesFileName
echo module.reportPublishing.active=0 >> $modulesFileName
echo module.processRuntime.active=1 >> $modulesFileName
echo module.processDesigner.active=1 >> $modulesFileName
echo module.filecenter.active=$MODULE_FILECENTER >> $modulesFileName
echo module.storage.active=1 >> $modulesFileName
echo module.news.active=$MODULE_NEWS >> $modulesFileName

# create config.properties
configFileName="$VDOC_HOME/config.properties"

# MYSQL 
java -cp $VDOC_HOME/java/PropertiesUpdater.jar PropertiesUpdater $configFileName

# apply config quick
cd $VDOC_HOME/configurator
$JAVA_HOME/bin/java -Dcom.axemble.vdoc.dev=true -jar $VDOC_HOME/configurator/Configurator-suite.jar console
cd $VDOC_HOME

$VDOC_HOME/startvdoc.sh

