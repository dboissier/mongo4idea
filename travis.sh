#!/usr/bin/env bash
./fetchIdea.sh

# Run the tests
mvn clean install

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    mvn clean
    rm -rf idea-IC
fi

# Return the build status
exit ${stat}