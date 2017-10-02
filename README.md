## xap-docker

This repository contains:

1. Dockerfile of [GigaSpaces XAP](http://www.gigaspaces.com/xap-real-time-transaction-processing/overview)
2. [Azure templates](azure-templates/README.md) to run XAP on Azure
3. [Kubernetes deployment configuration files](kubernetes-templates/README.md) to deploy XAP with Kubernetes

### Installation

1. Install [Docker](https://www.docker.com/).

2. Clone [xap-docker](https://github.com/xap/xap-docker.git)

3. cd xap-docker 

4. Build an image from Dockerfile: `docker build -t gigaspaces/xap:12.1.1 .`

### Run XAP data grid on single host with the network set to bridge 

#### XAP management node

    docker run --name xap-mgt-node -d -P -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 gsa.global.lus 0 gsa.lus 1 gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0

#### XAP compute node

    docker run --name xap-node -d -P -e XAP_LOOKUP_LOCATORS=<LOOKUP-LOCATORS> -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 gsa.global.lus 0 gsa.lus 0 gsa.global.gsm 0 gsa.gsm 0 gsa.gsc 1

[Lookup locators](http://docs.gigaspaces.com/xap120adm/network-unicast-discovery.html) should be set to HOST:4174. It can be found using the following command:

    docker inspect --format '{{ .NetworkSettings.IPAddress }}' xap-mgt-node
 
#### GS Webui

    docker run --name gs-webui -d -p 8099:8099 -e XAP_LOOKUP_LOCATORS=<LOOKUP-LOCATORS> -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 ./bin/gs-webui.sh
    
### Run XAP data grid in multi-host environment with the network set to host 

#### XAP management node

    docker run --name xap-mgt-node -d --net=host -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 gsa.global.lus 0 gsa.lus 1 gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0

#### XAP compute node

    docker run --name xap-node -d --net=host -e XAP_LOOKUP_LOCATORS=<XAP-MGT-HOST-IP>:4174 -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 gsa.global.lus 0 gsa.lus 0 gsa.global.gsm 0 gsa.gsm 0 gsa.gsc 1
 
#### GS Webui

    docker run --name gs-webui -d --net=host -e XAP_LOOKUP_LOCATORS=<XAP-MGT-HOST-IP>:4174 -e XAP_LICENSE_KEY=<XAP-LICENSE-KEY> gigaspaces/xap:12.1.1 ./bin/gs-webui.sh