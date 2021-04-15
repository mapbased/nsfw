#!/bin/bash
cd `dirname $0`
cd ..
mvn clean
mvn package
mvn  dependency:copy-dependencies -DoutputDirectory=lib