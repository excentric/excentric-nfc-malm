#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Build the application
cd "$PROJECT_ROOT" && ./gradlew assemble

# Define the JAR file path
JAR_FILE="$PROJECT_ROOT/malm-cli-app/build/libs/malm-cli-app.jar"

# Run the JAR file
echo "Running JAR file..."
java -jar $JAR_FILE "$@"

# Return to the original directory
cd - > /dev/null
