#!/usr/bin/env bash
# original file from https://github.com/go-lang-plugin-org/go-lang-idea-plugin
# thanks zolotov :)

ideaVersion="14.1"

if [ ! -d ./idea-IC ]; then
    # Get our IDEA dependency
    wget http://download.jetbrains.com/idea/ideaIC-${ideaVersion}.tar.gz

    # Unzip IDEA
    tar zxf ideaIC-${ideaVersion}.tar.gz
    rm -rf ideaIC-${ideaVersion}.tar.gz

    # Move the versioned IDEA folder to a known location
    ideaPath=$(find . -name 'idea-IC*' | head -n 1)
    cd ${ideaPath}
    mvn install:install-file Dfile=forms_rt.jar -DgroupId=com.intellij -DartifactId=forms_rt.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=openapi.jar -DgroupId=com.intellij -DartifactId=openapi.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=jna.jar -DgroupId=com.intellij -DartifactId=jna.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=util.jar -DgroupId=com.intellij -DartifactId=util.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=idea.jar -DgroupId=com.intellij -DartifactId=idea.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=extensions.jar -DgroupId=com.intellij -DartifactId=extensions.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=resources.jar -DgroupId=com.intellij -DartifactId=resources.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=resources_en.jar -DgroupId=com.intellij -DartifactId=resources_en.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=icons.jar -DgroupId=com.intellij -DartifactId=icons.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=trove4j.jar -DgroupId=com.intellij -DartifactId=trove4j.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=annotations.jar -DgroupId=com.intellij -DartifactId=annotations.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=jdom.jar -DgroupId=com.intellij -DartifactId=jdom.jar -Dversion=${ideaVersion} -Dpackaging=jar
    mvn install:install-file Dfile=swingx-core.jar -DgroupId=com.intellij -DartifactId=swingx-core-1.6.2.jar -Dversion=${ideaVersion} -Dpackaging=jar
fi