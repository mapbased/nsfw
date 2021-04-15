#!/bin/sh
# This will add jar to classpath


for j in ../lib/*.jar; do
        CLASSPATH=$j:$CLASSPATH;
done



#java parameters
DEFAULT_OPTS="-server  -Xmx2G "

#
$JAVA_HOME/bin/java -cp $CLASSPATH $DEFAULT_OPTS  -cp "../conf:../target/nsfw-1.0-SNAPSHOT.jar:$CLASSPATH" com.mapkc.nsfw.Main $@  &