echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:9092 1 20

# create the metrics topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic metrics \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create minutely aggregations topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic metrics-aggregates-60 \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create 10-minute aggregation topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic metrics-aggregates-600 \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create hourly aggregation topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic metrics-aggregates-3600 \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create daily aggregation topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic metrics-aggregates-86400 \
  --replication-factor 1 \
  --partitions 4 \
  --create

sleep infinity