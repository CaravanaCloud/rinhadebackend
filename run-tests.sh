#!/bin/bash
set -ex


echo "Building Image"
./publish-image.sh

echo "Startin backend"
./start-backend.sh

echo "Run stress test"
./