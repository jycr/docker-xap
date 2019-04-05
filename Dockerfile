# FROM scratch
# FROM alpine:3.9
# FROM openjdk:8-alpine
FROM gigaspaces/xap-enterprise:14.2

ARG DEPENDENCIES="\
    com/ibm/mq/com.ibm.mq.allclient/9.0.5.0/com.ibm.mq.allclient-9.0.5.0.jar \
    javax/jms/javax.jms-api/2.0.1/javax.jms-api-2.0.1.jar \
    com/sun/messaging/mq/fscontext/4.6-b01/fscontext-4.6-b01.jar \
"

ARG CLASSPATH_XAP=/opt/gigaspaces/lib/platform/ext
ARG CLASSPATH_PU=/opt/gigaspaces/lib/optional/pu-common
ARG GS_CONF_DIR=/opt/gigaspaces/config/gsa

RUN set -ex \
    && mkdir -p \
        "${CLASSPATH_XAP}" \
        "${GS_CONF_DIR}" \
        "${CLASSPATH_PU}"

COPY ./conf/* ${GS_CONF_DIR}/
COPY ./lib-ext/* ${CLASSPATH_XAP}/

RUN set -ex \
    && for ARTIFACT in ${DEPENDENCIES}; do \
           wget "http://search.maven.org/remotecontent?filepath=${ARTIFACT}" -O "${CLASSPATH_PU}/$(basename ${ARTIFACT})" ; \
       done \
    && rm -rf /var/lib/apt/lists/*
    
VOLUME ["${CLASSPATH_XAP}"]
VOLUME ["${CLASSPATH_PU}"]
VOLUME ["${GS_CONF_DIR}"]

RUN mkdir -p /usr/local/

# Add JACOCO
ENV JACOCO_HOME_DIR /usr/local/jacoco
ENV JACOCO_VERSION "0.8.1"
ENV JACOCO_DOWNLOAD_URL "http://search.maven.org/remotecontent?filepath=org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
RUN mkdir -p "${JACOCO_HOME_DIR}" \
  && wget "${JACOCO_DOWNLOAD_URL}" -O "$JACOCO_HOME_DIR/jacocoagent.jar"


# Add Yourkit (resources: https://www.yourkit.com/docs/java/help/docker.jsp )
## To activate, just add an environment variable:
## XAP_GSC_OPTIONS=-agentpath:/usr/local/YourKit-JavaProfiler/bin/linux-x86-64/libyjpagent.so=listen=all
ENV YOURKIT_HOME_DIR /usr/local/YourKit-JavaProfiler
ENV YOURKIT_VERSION "2018.04-docker"
ENV YOURKIT_DOWNLOAD_URL "https://www.yourkit.com/download/docker/YourKit-JavaProfiler-${YOURKIT_VERSION}.zip"
RUN apk add --update gcompat
RUN mkdir -p "${YOURKIT_HOME_DIR}" \
  && wget "${YOURKIT_DOWNLOAD_URL}" -O /tmp/YourKit-JavaProfiler.zip \
  && unzip /tmp/YourKit-JavaProfiler.zip -d /tmp \
  && mv /tmp/YourKit-JavaProfiler-*/* "${YOURKIT_HOME_DIR}/" \
  && rm -rf /tmp/YourKit-*

EXPOSE 10001-10009 20000-20001

CMD ["host", "run-agent", "--auto", "--custom", "gsc_1=1", "--custom", "gsc_2=1"]
