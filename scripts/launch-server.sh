#!/usr/bin/env bash

JAVA_HOME=/Users/vladislav/Library/Java/JavaVirtualMachines/openjdk-17.0.1/Contents/Home
DERBY_DRIVER_PATH=/Users/vladislav/Downloads/db-derby-10.15.2.0-lib

${JAVA_HOME}/bin/java \
	--module-path ../jars:${DERBY_DRIVER_PATH}/lib \
	--module ru.hse.edu.vmpendischuk.jigsaw.server/ru.hse.edu.vmpendischuk.jigsaw.server.JigsawServer