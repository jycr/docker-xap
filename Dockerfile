FROM gigaspaces/xap-enterprise:12.3


COPY lib-ext/* /opt/gigaspaces/lib/platform/ext/
COPY xap-application-deployer /opt/gigaspaces/tools/xap-application-deployer
