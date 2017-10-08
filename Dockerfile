FROM openjdk:8-jdk-slim
# JDK is needed to execute GS Webui

ENV XAP_VERSION 12.1.1
ENV XAP_BUILD_NUMBER 17100
ENV XAP_MILESTONE ga
ENV XAP_HOME_DIR /opt/xap
# Download XAP
ADD https://gigaspaces-repository-eu.s3.amazonaws.com/com/gigaspaces/xap/${XAP_VERSION}/${XAP_VERSION}/gigaspaces-xap-premium-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER}.zip /tmp/gigaspaces-xap-premium-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER}.zip

RUN set -ex \
    && unzip /tmp/gigaspaces-xap-premium-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER}.zip -d /tmp/xap_uncompress \
    && mv /tmp/xap_uncompress/gigaspaces-xap-premium-${XAP_VERSION}-${XAP_MILESTONE}-b${XAP_BUILD_NUMBER} $XAP_HOME_DIR \
    && rm -rf \
        /tmp/gigaspaces-xap-premium-*.zip \
        /tmp/xap_uncompress \
        ${XAP_HOME_DIR}/{examples,tools}/ \
        ${XAP_HOME_DIR}/START_HERE.htm \
        ${XAP_HOME_DIR}/NOTICE.md

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

WORKDIR ${XAP_HOME_DIR}

EXPOSE 10000-10100 9104 7102 4174 8099 8090

CMD ["./bin/gs-agent.sh"]