#!/bin/bash

VERSION=4.11
BUILD_DIR=.build
MODULES_DIR=../java_modules

mkdir -p $MODULES_DIR
/bin/rm -rf $BUILD_DIR
/bin/rm -f $MODULES_DIR/com.trollworks.toolkit*

mkdir $BUILD_DIR

javac -p $MODULES_DIR -d $BUILD_DIR $(find src -name '*.java')

jar --create --file $MODULES_DIR/com.trollworks.toolkit@${VERSION}.jar --module-version ${VERSION} -C $BUILD_DIR . -C resources .

zip -r -9 -q $MODULES_DIR/com.trollworks.toolkit@${VERSION}-src.zip src resources

/bin/rm -rf $BUILD_DIR
