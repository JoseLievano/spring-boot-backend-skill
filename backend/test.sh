#!/usr/bin/env sh

set -e

JAVA_21_HOME="/usr/lib/jvm/java-21-openjdk-amd64"

if [ -d "$JAVA_21_HOME" ]; then
  JAVA_COMMAND="${JAVA_HOME:-}/bin/java"

  if [ ! -x "$JAVA_COMMAND" ] || ! "$JAVA_COMMAND" -version 2>&1 | grep -q 'version "21'; then
    export JAVA_HOME="$JAVA_21_HOME"
  fi
fi

mvn -Dtest=com.agentForgeBackend.TestLauncher test
