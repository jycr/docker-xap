#!/usr/bin/env bash

__BASEDIR="$(readlink -f "$(dirname "$0")")";if [ -z "$__BASEDIR" ]; then echo "__BASEDIR: undefined";exit 1;fi

function main(){
    local CMD=$1
    shift

    local mainAppClass="xap.tools.applicationdeployer.Main"
    if [ "$CMD" = "undeploy" ]; then
        mainAppClass="xap.tools.applicationdeployer.Undeploy"
    fi

    ${JAVA_HOME}/bin/java -cp "$__BASEDIR/xap-application-deployer.jar":$(find "$__BASEDIR/../lib/"{platform,required} -name '*.jar' | tr '\n' ':') "$mainAppClass" "$@"
    return $?
}

main "$@"
exit $?

