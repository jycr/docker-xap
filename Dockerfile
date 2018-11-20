FROM gigaspaces/xap-enterprise:12.3.1 as LIB

FROM maven:3.5-jdk-8-alpine as BUILD

ENV XAP_HOME=/opt/gigaspaces

RUN mkdir -p /opt
COPY --from=LIB "$XAP_HOME" "$XAP_HOME"

COPY ./xap-application-deployer /tmp/xap-application-deployer/
RUN \
  chmod +x /tmp/xap-application-deployer/setupEnvProxy.sh \
  && /tmp/xap-application-deployer/setupEnvProxy.sh \
  && mvn -f /tmp/xap-application-deployer/pom.xml clean package





# FROM scratch
# FROM debian:stretch
# FROM buildpack-deps:stretch-curl
# FROM buildpack-deps:stretch-scm
# FROM openjdk:8
FROM gigaspaces/xap-enterprise:12.3.1

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

COPY --from=BUILD /tmp/xap-application-deployer/target/xap-application-deployer-*.jar "${GS_TOOLS_DIR}/xap-application-deployer.jar"
COPY --from=BUILD /tmp/xap-application-deployer/xap-application-deployer.sh "${GS_TOOLS_DIR}/"

RUN set -ex \
    && DEBIAN_FRONTEND=noninteractive \
    && apt-get update \
    && apt-get install -y \
        curl \
    && for ARTIFACT in ${DEPENDENCIES}; do \
           curl "http://search.maven.org/remotecontent?filepath=${ARTIFACT}" > "${CLASSPATH_PU}/$(basename ${ARTIFACT})" ; \
       done \
    && chmod +x "${GS_TOOLS_DIR}/xap-application-deployer.sh" \
    && rm -rf /var/lib/apt/lists/*
    
VOLUME ["${GS_TOOLS_DIR}"]
VOLUME ["${CLASSPATH_XAP}"]
VOLUME ["${CLASSPATH_PU}"]

RUN mkdir -p /usr/local/

# Add JACOCO
ENV JACOCO_HOME_DIR /usr/local/jacoco
ENV JACOCO_VERSION "0.8.1"
ENV JACOCO_DOWNLOAD_URL "http://search.maven.org/remotecontent?filepath=org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
RUN mkdir -p "${JACOCO_HOME_DIR}" \
  && curl -fSL "${JACOCO_DOWNLOAD_URL}" -o "$JACOCO_HOME_DIR/jacocoagent.jar"


# Add Yourkit (resources: https://www.yourkit.com/docs/java/help/docker.jsp )
## To activate, just add an environment variable:
## XAP_GSC_OPTIONS=-agentpath:/usr/local/YourKit-JavaProfiler/bin/linux-x86-64/libyjpagent.so=listen=all
ENV YOURKIT_HOME_DIR /usr/local/YourKit-JavaProfiler
ENV YOURKIT_VERSION "2018.04-docker"
ENV YOURKIT_DOWNLOAD_URL "https://www.yourkit.com/download/docker/YourKit-JavaProfiler-${YOURKIT_VERSION}.zip"
RUN mkdir -p "${YOURKIT_HOME_DIR}" \
  && curl "${YOURKIT_DOWNLOAD_URL}" --output /tmp/YourKit-JavaProfiler.zip \
  && unzip /tmp/YourKit-JavaProfiler.zip -d /tmp \
  && mv /tmp/YourKit-JavaProfiler-*/* "${YOURKIT_HOME_DIR}/" \
  && rm -rf /tmp/YourKit-*
EXPOSE 10001-10009
