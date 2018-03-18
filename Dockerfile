FROM openjdk:8-jdk-slim
# JDK is needed to execute GS Webui

ENV XAP_VERSION 12.2.1
ENV XAP_BUILD_NUMBER 18100
ENV XAP_MILESTONE ga
ENV XAP_HOME_DIR /opt/xap
ENV JACOCO_HOME_DIR /opt/jacoco

# Download XAP
ENV XAP_DOWNLOAD_URL "https://gigaspaces-releases-eu.s3.amazonaws.com/com/gigaspaces/xap/${XAP_VERSION}/${XAP_VERSION}/gigaspaces-xap-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER}.zip"
ENV JACOCO_VERSION="0.8.0"
ENV JACOCO_DOWNLOAD_URL="http://search.maven.org/remotecontent?filepath=org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"

ENV BUILD_PACKAGES=curl

RUN set -ex \
    && apt-get update && apt-get install -y \
           $BUILD_PACKAGES \
    && mkdir -p "$JACOCO_HOME_DIR" \
    && curl -fSL "${XAP_DOWNLOAD_URL}" -o /tmp/xap.zip \
    && curl -fSL "${JACOCO_DOWNLOAD_URL}" -o "$JACOCO_HOME_DIR/jacocoagent.jar" \
    && unzip /tmp/xap.zip -d /tmp/xap_unzip \
    && mv /tmp/xap_unzip/gigaspaces-xap-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER} $XAP_HOME_DIR \
    && rm -rf \
        /tmp/xap.zip \
        /tmp/xap_unzip \
        ${XAP_HOME_DIR}/{examples,tools}/ \
        ${XAP_HOME_DIR}/START_HERE.htm \
        ${XAP_HOME_DIR}/NOTICE.md \
    && apt-get remove --purge -y $BUILD_PACKAGES \
    && rm -rf /var/lib/apt/lists/*

ENV XAP_NIC_ADDRESS "#eth0:ip#"
ENV EXT_JAVA_OPTIONS "-Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=4174 -Dcom.gs.transport_protocol.lrmi.bind-port=10000-10100 -Dcom.gigaspaces.start.httpPort=9104 -Dcom.gigaspaces.system.registryPort=7102"
ENV XAP_GSM_OPTIONS "-Xms128m -Xmx128m"
ENV XAP_GSC_OPTIONS "-Xms128m -Xmx128m"
ENV XAP_LOOKUP_GROUPS xap

# GS webui
ENV XAP_WEBUI_OPTIONS "${EXT_JAVA_OPTIONS}"
ENV WEBUI_PORT 8099

RUN set -ex \
    && apt-get update && apt-get install -y \
        apache2 \
        curl \
        netcat-openbsd \
        procps \
        vim

COPY ./xap-manager.conf /etc/apache2/sites-available/
RUN a2enmod proxy_http \
    && a2ensite xap-manager.conf \
    && service apache2 stop

COPY docker-entrypoint.sh /docker-entrypoint.sh
COPY xap.sh /xap.sh
COPY setenv.sh /opt/xap/bin/setenv.sh

COPY lib-ext/* /opt/xap/lib/platform/ext/

RUN chmod +x \
    /opt/xap/bin/setenv.sh \
	/docker-entrypoint.sh \
	/xap.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

WORKDIR ${XAP_HOME_DIR}

EXPOSE 10000-10100 9104 7102 4174 8090 8091 8099

CMD ["/xap.sh"]
