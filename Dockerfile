FROM gigaspaces/xap-enterprise:12.3


COPY ./lib-ext/* /opt/gigaspaces/lib/platform/ext/
COPY ./xap-application-deployer/target/xap-application-deployer-0.1.0-SNAPSHOT.jar /opt/gigaspaces/tools/xap-application-deployer.jar
