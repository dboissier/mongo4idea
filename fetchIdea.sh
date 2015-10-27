#!/usr/bin/env bash
# original file from https://github.com/go-lang-plugin-org/go-lang-idea-plugin
# thanks zolotov :)

ideaVersion="14.1.5"

rm -rf ./idea-IC

# Get our IDEA dependency
wget http://download.jetbrains.com/idea/ideaIC-${ideaVersion}.tar.gz

# Unzip IDEA
tar -xzvf ideaIC-${ideaVersion}.tar.gz
rm -rf ideaIC-${ideaVersion}.tar.gz

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IC*' | head -n 1)
echo 'Found Intellij path:' + ${ideaPath}
mv ${ideaPath} ./idea-IC
cd ./idea-IC

# install IDEA dependencies 
IDEA_HOME=$(pwd)
libs=( "forms_rt" "openapi" "util" "idea" "resources" "resource_en" "swingx-core" "annotations" "extensions" "jna" "jdom" "icons")
for lib in "${libs[@]}"
do
    mvn install:install-file -Dfile=${IDEA_HOME}/lib/${lib}.jar -DgroupId=com.intellij -DartifactId=${lib} -Dversion=${ideaVersion} -Dpackaging=jar
done
