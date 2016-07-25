#!/bin/bash

cd /root/

git clone https://git.deepaknadig.com/deepak/OpenSec.git
cd OpenSec
mvn clean install

onos-app localhost install! /root/OpenSec/target/OpenSec-1.0.2.oar



