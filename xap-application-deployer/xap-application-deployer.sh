#!/bin/bash

__BASEDIR="$(readlink -f "$(dirname $0)")";if [ -z "$__BASEDIR" ]; then echo "__BASEDIR: undefined";exit 1;fi

function main(){
    local CMD=$1
    shift

    if [ "$CMD" = "undeploy" ]; then
        CMD=Undeploy
    else
        CMD=Main
    fi
    $JAVA_HOME/bin/java -cp "$__BASEDIR/xap-application-deployer.jar":$(find "$__BASEDIR/../lib/"{platform,required} -name '*.jar' | tr '\n' ':') xap.tools.applicationdeployer.$CMD "$@"
}

main "$@"
