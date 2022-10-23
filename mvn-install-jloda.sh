#!/bin/bash
cd ~/IdeaProjects/community/jloda/antbuild
ant clean
#ant -f build-noawt.xml
ant jar

mvn org.apache.maven.plugins:maven-install-plugin:3.0.1:install-file -Dfile=./jloda.jar -DgroupId=org.husonlab -DartifactId=jloda -Dversion=1.0-SNAPSHOT -Dpackaging=jar
