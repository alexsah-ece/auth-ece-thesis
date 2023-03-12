# ---- Publisher

# 100K messages -> 10K devices, 10 messages per device
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 10000 -t water -c 100000

# 240K messages -> 1K devices, 240 messages per device
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 10000000 -d 0 -s 1 -e 1000 -t water -c 240000


# ---- Aggregator

## -- 1-minute

docker run --label=aggregator --label=aggregator-60 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 60

docker run --label=performance-tracker --label=performance-tracker-60 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATES_PERFORMANCE_TRACKER \
 --desiredMessageCount 100000 --windowDurationSeconds 60

# 100K messages -> 10K devices
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 10000 -t water -c 100000 && \
  docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 100000 -d 2 -s 1 -e 1000 -t water -c 1000

docker exec -i kafka kafka-streams-application-reset --application-id metrics-aggregates-60 \
                                     --input-topics metrics \
                                     --bootstrap-servers kafka:9092 \
                                     --zookeeper zookeeper:32181 \
                                     --force

## -- 1-hour

docker run --label=aggregator --label=aggregator-3600 --network=host \
  auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 3600

docker run --label=performance-tracker --label=performance-tracker-600 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATES_PERFORMANCE_TRACKER \
 --desiredMessageCount 4000 --windowDurationSeconds 3600

docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 10000000 -d 0 -s 1 -e 1000 -t water -c 250000

docker exec -i kafka kafka-streams-application-reset --application-id metrics-aggregates-3600 \
                                     --input-topics metrics \
                                     --bootstrap-servers kafka:9092 \
                                     --zookeeper zookeeper:32181 \
                                     --force

# ---- Cassandra

docker run --label=aggregator --label=aggregator-60 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 60

# -- 1 consumer
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER \
  --windowDurationSeconds 60 --bucket DAYS --desiredMessageCount 100000

# 100K messages -> 10K devices
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 10000 -t water -c 100000 && \
  docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 100000 -d 2 -s 1 -e 1000 -t water -c 1000

docker exec -i kafka kafka-streams-application-reset --application-id metrics-aggregates-60 \
                                     --input-topics metrics \
                                     --bootstrap-servers kafka:9092 \
                                     --zookeeper zookeeper:32181 \
                                     --force


# -- 2 consumers
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER \
  --windowDurationSeconds 60 --bucket DAYS --desiredMessageCount 50000


# 101K messages -> 10K devices
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 10000 -t water -c 101000 && \
  docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 100000 -d 2 -s 1 -e 1000 -t water -c 1000


# -- 4 consumers
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER \
  --windowDurationSeconds 60 --bucket DAYS --desiredMessageCount 25000


# 102K messages -> 10K devices
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 10000 -t water -c 102000 && \
  docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 100000 -d 2 -s 1 -e 1000 -t water -c 1000