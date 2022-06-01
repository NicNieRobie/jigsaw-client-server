#!/usr/bin/env bash

JAVA_HOME=/Users/vladislav/Library/Java/JavaVirtualMachines/openjdk-17.0.1/Contents/Home
PATH_TO_FX=E:/Users/vladislav/Downloads/javafx-sdk-18.0.1

${JAVA_HOME}/bin/java \
	--module-path ../jars:${PATH_TO_FX}/lib \
	--module ru.hse.edu.vmpendischuk.jigsaw.client/ru.hse.edu.vmpendischuk.jigsaw.client.JigsawApplication