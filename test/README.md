# Running the Lucee Test Suite Locally

## Install Prerequisites

To run the lucee test suite, you'll need a few services on your machine:

1. Docker and docker-compose
2. Ant
3. Maven
4. Java JRE and JDK (minimum 1.8)

## Set up Databases / Services

Run the provided `docker-compose.yml` from the `test/` directory using `docker-compose up -d` to start up the necessary services:

```bash
cd test && docker-compose up -d
```

## Run Tests

Once in the test directory, run the test.sh bash script, which imports the test environment variables and starts the `ant` test runner:

```bash
./test.sh
```