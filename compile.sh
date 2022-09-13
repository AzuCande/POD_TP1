#!/bin/bash

TP_PATH=/Users/srosati/University/POD/POD_TP1
cd $TP_PATH

mvn clean install

cd server/target
tar -xzf tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz
cd tpe1-g6-server-1.0-SNAPSHOT
chmod +x *.sh

cd $TP_PATH/client/target
tar -xzf tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz
cd tpe1-g6-client-1.0-SNAPSHOT
chmod +x *.sh
cd $TP_PATH