#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Build the application
cd "$PROJECT_ROOT" && ./gradlew assemble

# Check if native binary exists
NATIVE_BINARY="$PROJECT_ROOT/malm-cli-app/build/native/nativeCompile/malm-cli-app"
JAR_FILE="$PROJECT_ROOT/malm-cli-app/build/libs/malm-cli-app.jar"

if [ -f "$NATIVE_BINARY" ]; then
    echo "Running native binary..."
    $NATIVE_BINARY "$@"
else
    echo "Native binary not found, running JAR file..."
    java -jar $JAR_FILE "$@"
fi

# Return to the original directory
cd - > /dev/null
