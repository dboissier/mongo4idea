#!/usr/bin/env bash

cd ./idea-IC
export IDEA_HOME=$(pwd)
cd ..

# Run the tests
export DISPLAY=:99.0
/sbin/start-stop-daemon --start --quiet --pidfile /tmp/custom_xvfb_99.pid --make-pidfile --background --exec /usr/bin/Xvfb -- :99 -ac -screen 0 1280x1024x16 +extension RANDR
mvn clean install

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    mvn clean
    rm -rf idea-IC
fi

# Return the build status
exit ${stat}
