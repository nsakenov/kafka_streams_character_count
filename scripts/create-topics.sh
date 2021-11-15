echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:9092 1 20

# create the pulse-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic character-count-input \
  --replication-factor 1 \
  --partitions 3 \
  --create

# create the body-temp-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic character-count-output \
  --replication-factor 1 \
  --partitions 3 \
  --create

sleep infinity