#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

echo "Building native image for malm-cli-app..."
cd "$PROJECT_ROOT" && ./gradlew :malm-cli-app:nativeCompile

if [ $? -eq 0 ]; then
    echo "Native image built successfully!"
    echo "You can find it at: $PROJECT_ROOT/malm-cli-app/build/native/nativeCompile/malm-cli-app"
    echo "Run it with: $SCRIPT_DIR/cli-app.sh"
else
    echo "Failed to build native image. Please check the logs for errors."
    exit 1
fi

# Return to the original directory
cd - > /dev/null
