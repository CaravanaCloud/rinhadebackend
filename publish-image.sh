#!/bin/bash
set -ex

./mvnw package -Dnative -DskipTests
docker build -f src/main/docker/Dockerfile.native-micro -t caravanacloud/rinhadebackend .
docker push caravanacloud/rinhadebackend

echo 'docker run -i --rm -p 9090:9090 caravanacloud/rinhadebackend'
