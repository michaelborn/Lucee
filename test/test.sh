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

# Run the tests
ant -f ../loader/build.xml core
# mvn -e -f ../loader/pom.xml clean deploy