#!/bin/bash

set -ex

git checkout settings.xml
sed -i 's/${nexusUser}/'"$NEXUS_USER"'/' settings.xml
sed -i 's/${nexusPassword}/'"$NEXUS_PASSWORD"'/' settings.xml

docker $DOCKER_OPTS build -t p8/pg-op-mon .

docker $DOCKER_OPTS tag p8/pg-op-mon hub.predic8.de/p8/pg-op-mon
docker $DOCKER_OPTS tag p8/pg-op-mon p8/pg-op-mon:$BUILD_NUMBER
docker $DOCKER_OPTS tag p8/pg-op-mon hub.predic8.de/p8/pg-op-mon:$BUILD_NUMBER
docker $DOCKER_OPTS push hub.predic8.de/p8/pg-op-mon:latest
docker $DOCKER_OPTS push hub.predic8.de/p8/pg-op-mon:$BUILD_NUMBER


cd kubernetes

cp template.yaml tmp.yaml
sed -i 's/$BUILD_NUMBER/'$BUILD_NUMBER'/' tmp.yaml
cat tmp.yaml
kubectl -s https://congo.predic8.de:8443 apply -f tmp.yaml