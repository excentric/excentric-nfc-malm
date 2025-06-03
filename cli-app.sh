#!/bin/bash

# Build the application
./gradlew assemble

# Check if native binary exists
NATIVE_BINARY="./malm-cli-app/build/native/nativeCompile/malm-cli-app"
JAR_FILE="./malm-cli-app/build/libs/malm-cli-app.jar"

if [ -f "$NATIVE_BINARY" ]; then
    echo "Running native binary..."
    $NATIVE_BINARY "$@"
else
    echo "Native binary not found, running JAR file..."
    java -jar $JAR_FILE "$@"
fi
