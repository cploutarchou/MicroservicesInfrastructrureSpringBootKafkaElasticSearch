#!/bin/bash
# check-config-server-started.sh

# Check the operating system
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    apt-get update -y
    yes | apt-get install curl
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    brew update
    yes | brew install curl
elif [[ "$OSTYPE" == "win"* ]]; then
    # Windows
    choco install curl
fi

# Send a GET request to the config server's health endpoint
curlResult=$(curl -s -o /dev/null -I -w "%{http_code}" http://config-server:8888/actuator/health)

echo "result status code:" "$curlResult"

# Wait until the config server is up and running
while [[ ! $curlResult == "200" ]]; do
  >&2 echo "Config server is not up yet!"
  sleep 2
  curlResult=$(curl -s -o /dev/null -I -w "%{http_code}" http://config-server:8888/actuator/health)
done

# Launch a Cloud Native Buildpack (CNB) lifecycle process
./cnb/lifecycle/launcher
