#!/usr/bin/env bash
set -e

./gradlew clean
./gradlew build
(cd infrastructure && cdk deploy --all --require-approval never --outputs-file outputs.json )