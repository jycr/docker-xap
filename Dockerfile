FROM openjdk:8-jdk-slim
# JDK is needed to execute GS Webui

ENV PRODUCT_NAME=insightedge
ENV PRODUCT_VERSION 12.3.0
ENV PRODUCT_BUILD ga-b19000
ENV PRODUCT_HOME_DIR /opt/xap

# Download Product URL
ENV DOWNLOAD_URL "https://gigaspaces-releases-eu.s3.amazonaws.com/com/gigaspaces/${PRODUCT_NAME}/${PRODUCT_VERSION}/${PRODUCT_VERSION}/gigaspaces-${PRODUCT_NAME}-${PRODUCT_VERSION}-${PRODUCT_BUILD}.zip"

ENV BUILD_PACKAGES=curl

RUN set -ex \
    && apt-get update && apt-get install -y \
           $BUILD_PACKAGES \
    && curl -fSL "${DOWNLOAD_URL}" -o /tmp/_product.zip \
    && unzip /tmp/_product.zip -d /tmp/_product_unzip \
    && mv /tmp/_product_unzip/gigaspaces-${PRODUCT_NAME}-${PRODUCT_VERSION}-${PRODUCT_BUILD} $PRODUCT_HOME_DIR \
    && rm -rf \
        /tmp/_product.zip \
        /tmp/_product_unzip \
        ${PRODUCT_HOME_DIR}/{examples,tools}/ \
        ${PRODUCT_HOME_DIR}/START_HERE.htm \
        ${PRODUCT_HOME_DIR}/NOTICE.md \
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

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

WORKDIR ${PRODUCT_HOME_DIR}

EXPOSE 10000-10100 9104 7102 4174 8099 8090

CMD ["./bin/gs-agent.sh"]
