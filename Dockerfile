# Version: 0.0.1
FROM ubuntu:14.04

MAINTAINER Visiativ Software "rauburtin@visiativ.com"

RUN locale-gen en_US.UTF-8 
ENV LANG en_US.UTF-8 
ENV VDOC_VERSION 14.2 
ENV VDOC_HOME /opt/VDocPlatform 
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# VDoc Configuration
# VDoc process and DataStockroom is force to enable 
# DM connector is force to disable because he need a .dll
# Report is force to disable 

ENV MODULE_EASYSITE 0
ENV MODULE_EDITORIALS 0
ENV MODULE_LINKS 0
ENV MODULE_FORUMS 0
ENV MODULE_NEWS 0
ENV MODULE_POLLS 0
ENV MODULE_FILECENTER 1

# some base configuration
ENV WEBAPP vdoc

# Add VDoc 
RUN mkdir -p /opt
WORKDIR /opt

# Get VDoc installer for Linux x64 from .iso
ADD ./vdoc/install-linux-x64.run install-linux-x64.run
RUN chmod +x install-linux-x64.run
RUN ./install-linux-x64.run --mode unattended \
	--unattendedmodeui minimal \
	--moduleNews $MODULE_NEWS \
	--moduleEditorials $MODULE_EDITORIALS \
	--moduleForums $MODULE_FORUMS \
	--moduleLinks $MODULE_LINKS \
	--modulePolls $MODULE_POLLS \
	--moduleFilecenter $MODULE_FILECENTER \
	--moduleEasysite $MODULE_EASYSITE \
	--moduleUniverse 1 \
	--moduleReportPublishing 0 \
	--moduleReportImport 0 \
	--moduleConnectorDM 0 \
	--moduleConnectorVDPD 0 \
	--prefix $VDOC_HOME 
	
RUN rm install-linux-x64.run

# Add OpenJDK and it's env variable
ENV JAVA_HOME $VDOC_HOME/JDKPart64
# Add java to path
RUN ln -s $JAVA_HOME/bin/java /bin/java

WORKDIR $VDOC_HOME

# Make a first applyconfig
RUN chmod +x applyconfig.sh && \
	$VDOC_HOME/applyconfig.sh

# docker_starter.sh need to have update some java properties file (this is do in Java)
RUN mkdir -p $VDOC_HOME/java/
ADD scripts/java/PropertiesUpdater.java $VDOC_HOME/java/PropertiesUpdater.java
RUN $JAVA_HOME/bin/javac $VDOC_HOME/java/PropertiesUpdater.java && \
	cd $VDOC_HOME/java/ && \
	$JAVA_HOME/bin/jar cvf PropertiesUpdater.jar * &&\
	cd $VDOC_HOME

ADD scripts/docker_starter.sh $VDOC_HOME/docker_starter.sh
RUN chmod +x $VDOC_HOME/docker_starter.sh

EXPOSE 8080

VOLUME ["/opt/VDocPlatform/contentstore"]

# The default command is not startvdoc.sh
# We want to have ability to change some configuration at start up and not only during docker build 
ENTRYPOINT ["/opt/VDocPlatform/docker_starter.sh"]

