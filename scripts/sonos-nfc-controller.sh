#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Change to the malm-nfc directory
cd "$PROJECT_ROOT/malm-nfc"

export PATH=$PROJECT_ROOT/.gradle/nodejs/node-v21.6.2-linux-x64/bin:$PATH
# Run the sonos-nfc-controller npm script
npm run sonos-nfc-controller -- "$@"

# Return to the original directory
cd - > /dev/null
