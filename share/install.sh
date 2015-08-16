#!/bin/bash

DEV_SERVER=192.168.0.106
INSTALL_DIR=/var/lib/tomcat7/webapps/share/WEB-INF/lib

USERNAME=admin
PASSWORD=setup

gradle clean build
find . -iname '*jar' -type f -exec scp '{}' root@$DEV_SERVER:$INSTALL_DIR \;
curl -u $USERNAME:$PASSWORD http://$DEV_SERVER:8080/manager/text/reload?path=/share
