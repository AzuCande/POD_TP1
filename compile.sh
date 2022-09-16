#!/bin/bash

TP_PATH=/Users/patrick/Desktop/ITBA/POD/TP1/POD_TP1
cd $TP_PATH

mvn clean install

cd server/target
tar -xzf tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz
cd tpe1-g6-server-1.0-SNAPSHOT
chmod +x run-registry run-server

cd $TP_PATH/client/target
tar -xzf tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz
cd tpe1-g6-client-1.0-SNAPSHOT
chmod +x run-admin run-notifications run-seatAssign run-seatMap
cd $TP_PATH