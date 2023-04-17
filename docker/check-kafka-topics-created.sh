#!/bin/bash
# check-kafka-topics-created.sh

# Check the operating system
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    apt-get update -y
    yes | apt-get install kafkacat
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    brew update
    yes | brew install kafkacat
elif [[ "$OSTYPE" == "win"* ]]; then
    # Windows
    choco install kafkacat
fi

# Check if the Kafka topic has been created
kafkacatResult=$(kafkacat -L -b kafka-broker-1:9092)

echo "kafkacat result:" $kafkacatResult

while [[ ! $kafkacatResult == *"twitter-topic"* ]]; do
  >&2 echo "Kafka topic has not been created yet!"
  sleep 2
  kafkacatResult=$(kafkacat -L -b kafka-broker-1:9092)
done

# Launch a Cloud Native Buildpack (CNB) lifecycle process
./cnb/lifecycle/launcher
