#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Build the application
cd "$PROJECT_ROOT" && ./gradlew assemble

# Define the native binary path
NATIVE_BINARY="$PROJECT_ROOT/malm-cli-app/build/native/nativeCompile/malm-cli-app"

# Check if native binary exists
if [ -f "$NATIVE_BINARY" ]; then
    echo "Running native binary..."
    $NATIVE_BINARY "$@"
else
    echo "Native binary not found. Please build it first using build-native.sh"
    exit 1
fi

# Return to the original directory
cd - > /dev/null
