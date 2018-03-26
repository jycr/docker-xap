#!/bin/bash

echo "Start Apache HTTP Server"
/etc/init.d/apache2 restart

${XAP_HOME_DIR}/bin/gs-agent.sh $*