#!/bin/bash
# ***********************************************************************************************************
# * This script is used to initialize common environment to GigaSpaces XAP Server.                          *
# * It is highly recommended NOT TO MODIFY THIS SCRIPT, to simplify future upgrades.                        *
# * If you need to override the defaults, please modify setenv-overrides.sh or set                          *
# * the XAP_SETTINGS_FILE environment variable to your custom script.                                       *
# * For more information see http://docs.gigaspaces.com/xap/12.2/started/common-environment-variables.html *
# ***********************************************************************************************************
#Load overrides settings.
DIRNAME=$(dirname ${BASH_SOURCE[0]})

if [ -z ${XAP_SETTINGS_FILE} ]; then
    export XAP_SETTINGS_FILE=${DIRNAME}/setenv-overrides.sh
fi

if [ -f ${XAP_SETTINGS_FILE} ]; then
    source ${XAP_SETTINGS_FILE}
fi

if [ -z "${JAVA_HOME}" ]; then
  	echo "The JAVA_HOME environment variable is not set. Using the java that is set in system path."
	export JAVACMD=java
	export JAVACCMD=javac
	export JAVAWCMD=javaw
else
	export JAVACMD="${JAVA_HOME}/bin/java"
	export JAVACCMD="${JAVA_HOME}/bin/javac"
	export JAVAWCMD="${JAVA_HOME}/bin/javaw"
fi


if [ ! -z "${JACOCO_ARGS}" ]; then
	JACOCO_CMD="-javaagent:/opt/jacoco/jacocoagent.jar=${JACOCO_ARGS}"
	EXT_JAVA_OPTIONS="${JACOCO_CMD} ${EXT_JAVA_OPTIONS}"
fi

export XAP_HOME=${XAP_HOME=`(cd $DIRNAME/..; pwd )`}
export XAP_NIC_ADDRESS=${XAP_NIC_ADDRESS="`hostname`"}
export XAP_SECURITY_POLICY=${XAP_SECURITY_POLICY=${XAP_HOME}/policy/policy.all}
export XAP_LOGS_CONFIG_FILE=${XAP_LOGS_CONFIG_FILE=${XAP_HOME}/config/log/xap_logging.properties}

export XAP_GSC_OPTIONS=${XAP_GSC_OPTIONS=-Xms512m -Xmx512m}
export XAP_MANAGER_OPTIONS=${XAP_MANAGER_OPTIONS=-Xmx512m}
export XAP_GSM_OPTIONS=${XAP_GSM_OPTIONS=-Xmx512m}
export XAP_GSA_OPTIONS=${XAP_GSA_OPTIONS=-Xmx512m}
export XAP_LUS_OPTIONS=${XAP_LUS_OPTIONS=-Xmx512m}
export XAP_ESM_OPTIONS=${XAP_ESM_OPTIONS=-Xmx512m}
export XAP_CLI_OPTIONS=${XAP_CLI_OPTIONS=-Xmx512m}
export XAP_GUI_OPTIONS=${XAP_GUI_OPTIONS=-Xmx512m}
export XAP_WEBUI_OPTIONS=${XAP_WEBUI_OPTIONS=-Xmx512m}


export XAP_OPTIONS="-Djava.util.logging.config.file=${XAP_LOGS_CONFIG_FILE} -Djava.security.policy=${XAP_SECURITY_POLICY} -Djava.rmi.server.hostname=${XAP_NIC_ADDRESS} -Dcom.gs.home=${XAP_HOME}"
export EXT_LD_LIBRARY_PATH=${EXT_LD_LIBRARY_PATH=}

if [ -z "${JAVA_OPTIONS}" ]; then
   JAVA_OPTIONS=`${JAVACMD} -cp "${XAP_HOME}/lib/required/xap-datagrid.jar" com.gigaspaces.internal.utils.OutputJVMOptions`
   JAVA_OPTIONS="${JAVA_OPTIONS} ${EXT_JAVA_OPTIONS}"
fi
export JAVA_OPTIONS

export GS_JARS="${XAP_HOME}"/lib/platform/ext/*:${XAP_HOME}:"${XAP_HOME}"/lib/required/*:"${XAP_HOME}"/lib/optional/pu-common/*:"${XAP_CLASSPATH_EXT}"
export COMMONS_JARS="${XAP_HOME}"/lib/platform/commons/*
export JDBC_JARS="${XAP_HOME}"/lib/optional/jdbc/*
export SIGAR_JARS="${XAP_HOME}"/lib/optional/sigar/*
export SERVICE_GRID_JARS="${XAP_HOME}"/lib/platform/service-grid/*
export UI_JARS="${XAP_HOME}"/lib/platform/ui/*:"${XAP_HOME}"/lib/platform/poi/*:"${XAP_HOME}"/lib/platform/benchmark/*:"${XAP_HOME}"/lib/optional/jms/*:"${XAP_HOME}"/lib/optional/map/*:${SERVICE_GRID_JARS}:${GS_JARS}
export MEMORYXTEND_JARS="${XAP_HOME}"/lib/optional/memoryxtend/mapdb/*:"${XAP_HOME}"/lib/optional/memoryxtend/rocksdb/*
export METRICS_JARS="${XAP_HOME}"/lib/optional/metrics/*
export SPRING_JARS="${XAP_HOME}"/lib/optional/spring/*:"${XAP_HOME}"/lib/optional/security/*

if [ "${VERBOSE}" = "true" ] ; then
	echo ===============================================================================
	echo GigaSpaces XAP environment verbose information
	echo XAP_HOME: $XAP_HOME
	echo XAP_NIC_ADDRESS: $XAP_NIC_ADDRESS
	echo XAP_LOOKUP_GROUPS: $XAP_LOOKUP_GROUPS
	echo XAP_LOOKUP_LOCATORS: $XAP_LOOKUP_LOCATORS
	echo GS_JARS: $GS_JARS
	echo
	echo JAVA_HOME: $JAVA_HOME
	echo JAVA_VM_NAME: $JAVA_VM_NAME
	echo JAVA_VERSION: $JAVA_VERSION
	echo EXT_JAVA_OPTIONS: $EXT_JAVA_OPTIONS
	echo JAVA_OPTIONS: $JAVA_OPTIONS
	echo ===============================================================================
fi
