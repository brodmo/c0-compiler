#!/usr/bin/env sh
BIN_DIR="$(dirname "$0")/build/install/compiler/bin"
export JAVA_OPTS="-Xss4m"
$BIN_DIR/compiler "$@"
