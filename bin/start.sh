#!/bin/bash
cd $(dirname $0)
BIN_DIR=$(pwd)
cd ..
DEPLOY_DIR=$(pwd)
CONF_DIR=$DEPLOY_DIR/conf
SERVER_NAME=$(sed '/app.name/!d;s/.*=//' conf/config.properties | tr -d '\r')
#`sed '/application.name/!d;s/.*=//' conf/config.properties | tr -d '\r'`
#SERVER_PROTOCOL=`sed '/protocol.name/!d;s/.*=//' conf/config.properties | tr -d '\r'`
SERVER_PORT=9080

LOGS_DIR=$(sed '/log.base/!d;s/.*=//' conf/config.properties | tr -d '\r')

if [ -z "$SERVER_NAME" ]; then
  SERVER_NAME=$(hostname)
fi

PIDS=$(ps -f | grep java | grep "mapkc" | awk '{print $2}')
if [ -n "$PIDS" ]; then
  echo "ERROR: The $SERVER_NAME already started!"
  echo "PID: $PIDS"
  exit 1
fi

if [ -n "$SERVER_PORT" ]; then
  SERVER_PORT_COUNT=$(netstat -tln | grep $SERVER_PORT | wc -l)
  if [ $SERVER_PORT_COUNT -gt 0 ]; then
    echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
    exit 1
  fi
fi

STDOUT_FILE=$LOGS_DIR/stdout.log

echo $STDOUT_FILE



for j in ../lib/*.jar; do
        CLASSPATH=$j:$CLASSPATH;
done



#java parameters
DEFAULT_OPTS="-server  -Xmx2G "

#
nohup $JAVA_HOME/bin/java -cp $CLASSPATH $DEFAULT_OPTS  -cp "../conf:../target/nsfw-1.0-SNAPSHOT.jar:$CLASSPATH" com.mapkc.nsfw.Main $@  &

#echo -e "Starting the $SERVER_NAME ...\c"
#nohup $JAVA_HOME/bin/java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS $JAVA_GC_OPTS -cp $CONF_DIR:$LIB_JARS com.mapkc.eds.Main >/dev/null &

COUNT=0
while [ $COUNT -lt 1 ]; do
  echo -e ".\c"
  sleep 1
  if [ -n "$SERVER_PORT" ]; then
    if [ "$SERVER_PROTOCOL" == "dubbo" ]; then
      COUNT=$(echo status | nc -i 1 127.0.0.1 $SERVER_PORT | grep -c OK)
    else
      COUNT=$(netstat -an | grep $SERVER_PORT | wc -l)
    fi
  else
    COUNT=$(ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l)
  fi
  if [ $COUNT -gt 0 ]; then
    break
  fi
done

echo "OK!"
PIDS=$(ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}')
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"
