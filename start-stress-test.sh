#!/bin/bash

pushd "$HOME/projects/rinha-de-backend-2023-q3/stress-test"
echo "Executing stress test"
source ./run-test.sh
popd