#!/bin/bash
set -ex


docker-compose down
docker-compose rm -f
docker-compose up --build # --detach