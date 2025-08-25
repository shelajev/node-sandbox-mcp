#!/bin/sh

set -ex

./mvnw package -DskipTests=true -Dquarkus.container-image.build=true
docker tag shelajev/node-sandbox:1.0.0-SNAPSHOT olegselajev241/node-sandbox:latest
docker push olegselajev241/node-sandbox:latest