#!/bin/bash

echo "Building native image for malm-cli-app..."
./gradlew :malm-cli-app:nativeCompile

if [ $? -eq 0 ]; then
    echo "Native image built successfully!"
    echo "You can find it at: ./malm-cli-app/build/native/nativeCompile/malm-cli-app"
    echo "Run it with: ./cli-app.sh"
else
    echo "Failed to build native image. Please check the logs for errors."
    exit 1
fi
