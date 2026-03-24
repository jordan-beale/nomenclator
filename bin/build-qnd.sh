#!/usr/bin/env bash

SCRIPT_PATH=$(dirname "$0")

set -Eeu

echo Building Nomenclator the quick and dirty way...
mvn --batch-mode -P-docker-multiplatform -DskipTests=true clean install
docker image prune -f
echo Built Nomenclator the quick and dirty way.
