#!/bin/sh
DIR=`dirname "$0"`
JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}
exec "$JAVA_HOME/bin/java" -Xmx64m -cp "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
