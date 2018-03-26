#!/bin/bash
set -e

echo "Start Apache HTTP Server"
/etc/init.d/apache2 restart
/etc/init.d/apache2 status

if [[ -z "$XAP_LICENSE_KEY" ]]; then
	echo "Please set 'XAP_LICENSE_KEY' environment variable"; exit 2
fi

readonly first_param=${1#\"}
if [[ "${first_param:0:1}" = '-' || "${first_param:0:3}" = 'gsa' ]]; then
	set -- ./bin/gs-agent.sh "$@"
fi

: ${XAP_LOOKUP_LOCATORS="$(hostname --ip-address):4174"}

exec env EXT_JAVA_OPTIONS="-Dcom.gs.licensekey=$XAP_LICENSE_KEY $EXT_JAVA_OPTIONS" XAP_WEBUI_OPTIONS="-Dcom.gs.licensekey=$XAP_LICENSE_KEY $XAP_WEBUI_OPTIONS" XAP_LOOKUP_LOCATORS="$XAP_LOOKUP_LOCATORS" "$@"