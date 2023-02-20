#!/usr/bin/env bash

###
# Lucee Test script
# 
# 1. Start up docker services via `docker-compose up -d`
# 2. Run the tests via `./test.sh`
###

# Import .env and export to environment
# Useful for testing MSSQL, MYSQL with secret credentials
# @see https://stackoverflow.com/a/30969768
set -o allexport
source .env
set +o allexport

###
# RUN TEST SUITE
# includes all shell arguments passed to this script.
# So the user can specify test filters, etc. to the ant script:
# `test.sh -DtestLabels="orm"` will run `ant -DtestLabels="s3"`
# 
# @see https://docs.lucee.org/guides/working-with-source/build-from-source.html#build-performance-tips
###
ant -f ../loader/build.xml core "$@"
# mvn -e -f ../loader/pom.xml clean deploy