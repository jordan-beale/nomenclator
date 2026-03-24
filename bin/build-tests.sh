#!/usr/bin/env bash

SCRIPT_PATH=$(dirname "$0")

set -Eeu

echo Building Nomenclator...
mvn --batch-mode -P-docker-multiplatform clean install
docker image prune -f
echo Built Nomenclator.
