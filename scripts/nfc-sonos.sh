#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Change to the malm-nfc directory
cd "$PROJECT_ROOT/malm-nfc"

# Run the read-tag npm script
npm run read-tag -- "$@"

# Return to the original directory
cd - > /dev/null
