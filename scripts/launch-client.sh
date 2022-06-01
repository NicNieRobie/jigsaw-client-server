#!/usr/bin/env bash

JAVA_HOME=C:/Users/Vlad/.jdks/openjdk-17.0.2
PATH_TO_FX=E:/sdk/javafx-sdk-18.0.1

${JAVA_HOME}/bin/java \
	--module-path jars:${PATH_TO_FX}/lib \
	--module ru.hse.edu.vmpendischuk.jigsaw.client/ru.hse.edu.vmpendischuk.jigsaw.client.JigsawApplication