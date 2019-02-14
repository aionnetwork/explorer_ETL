#!/bin/bash

ETL_DIR=/home/aion/deployment/etl

if [ -s "$ETL_DIR/etl.pid" ]; then
    pid=$(cat $ETL_DIR/etl.pid)
    sudo kill -TERM $pid
    sudo kill -KILL $pid

fi
sudo su aion
source $ETL_DIR/../.env
$ETL_DIR/gradlew run  >$ETL_DIR/logs/etl.log &
echo $! >$ETL_DIR/etl.pid
#byobu send-keys -t 'ETL2' "tail -f $ETL_DIR/etl.log" 'C-m'

