# FROM scratch
# FROM debian:stretch
# FROM buildpack-deps:stretch-curl
# FROM buildpack-deps:stretch-scm
# FROM openjdk:8
FROM gigaspaces/xap-enterprise:12.3.1

ARG BUILD_PACKAGES="\
    curl \
"

ARG RUN_PACKAGES="\
"

ARG DEPENDENCIES="\
    com/ibm/mq/com.ibm.mq.allclient/9.0.5.0/com.ibm.mq.allclient-9.0.5.0.jar \
    javax/jms/javax.jms-api/2.0.1/javax.jms-api-2.0.1.jar \
    com/sun/messaging/mq/fscontext/4.6-b01/fscontext-4.6-b01.jar \
"

ARG CLASSPATH_XAP=/opt/gigaspaces/lib/platform/ext
ARG CLASSPATH_PU=/opt/gigaspaces/lib/optional/pu-common
ARG GS_TOOLS_DIR=/opt/gigaspaces/tools

RUN set -ex \
    && mkdir -p \
        "${GS_TOOLS_DIR}" \
        "${CLASSPATH_XAP}" \
        "${CLASSPATH_PU}"

COPY ./lib-ext/* ${CLASSPATH_XAP}/
COPY ./xap-application-deployer/target/xap-application-deployer-*.jar "${GS_TOOLS_DIR}/xap-application-deployer.jar"
COPY ./xap-application-deployer/xap-application-deployer.sh "${GS_TOOLS_DIR}/"

RUN set -ex \
    && DEBIAN_FRONTEND=noninteractive \
    && apt-get update \
    && apt-get install -y \
        $BUILD_PACKAGES \
        $RUN_PACKAGES \
    && for ARTIFACT in ${DEPENDENCIES}; do \
           curl "http://search.maven.org/remotecontent?filepath=${ARTIFACT}" > "${CLASSPATH_PU}/$(basename ${ARTIFACT})" ; \
       done \
    && apt-get remove --purge -y \
        $BUILD_PACKAGES \
    && chmod +x "${GS_TOOLS_DIR}/xap-application-deployer.sh" \
    && rm -rf /var/lib/apt/lists/*
    
VOLUME ["${GS_TOOLS_DIR}"]
VOLUME ["${CLASSPATH_XAP}"]
VOLUME ["${CLASSPATH_PU}"]
