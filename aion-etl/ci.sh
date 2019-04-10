#!/bin/bash
pid=$(lsof -i:7091 -sTCP:LISTEN -t);
ETL_DIR=/home/aion/deployment/etl

if [[ -n ${pid} ]]; then

    sudo kill -TERM $pid
    sleep 10s
    sudo kill -KILL $pid

fi
sudo su aion
source $ETL_DIR/../.env
$ETL_DIR/gradlew run  >$ETL_DIR/logs/etl.log &
echo $! >$ETL_DIR/etl.pid
#byobu send-keys -t 'ETL2' "tail -f $ETL_DIR/etl.log" 'C-m'

