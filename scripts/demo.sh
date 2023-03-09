# ---- System setup

# start all base infrastructure containers
docker compose up -d

# create cassandra tables
docker exec cassandra cqlsh -f /scripts/create-tables.cql

# login into cassandra container
docker exec -ti cassandra cqlsh

# ---- Publisher

# bash? or custom manual command?

# publish electricity for 5 households
./kafka-replay.sh -d 0 -r 5 -h 5 -t 1

# publish water for 5 households
./kafka-replay.sh -d 0 -r 5 -h 5 -t 2

# publish gas for 5 households
./kafka-replay.sh -d 0 -r 5 -h 5 -t 3


# ---- Console consumer

# Can run as many consumers as we want, by repeating the same command. Automatic re-balance will take place if we
# add more, as they will belong in the same group.
docker run --label=console-consumer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CONSOLE_CONSUMER

# ---- Aggregator

# To run more instances of the same aggregator, we need to provide a different --clientId, if they run in the same host.
# However, if we run as containerized Kafka streams apps, we can omit the --clientId option, as there will be no race
# condition for the state dir (each container has its own filesystem).

# 1-minute aggregates
docker run --label=aggregator --label=aggregator-60 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 60

# 10-minute aggregates
docker run --label=aggregator --label=aggregator-600 --network=host \
  auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 600

# 1-hour aggregates
docker run --label=aggregator --label=aggregator-3600 --network=host \
  auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 3600

# 1-day aggregates
docker run --label=aggregator --label=aggregator-86400 --network=host \
  auth-ece/consumer:0.1.0 --applicationType AGGREGATOR --sourceTopic metrics --windowDurationSeconds 86400

# ---- Cassandra writer

# Can run as many consumers as we want, by repeating the same command. Automatic re-balance will take place if we
# add more, as they will belong in the same group.

# 1-minute aggregates
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER --windowDurationSeconds 60 --bucket DAYS

# 10 minute aggregates - use day as partition bucket
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER --windowDurationSeconds 600 --bucket DAYS


# 1-hour aggregates - use month as partition bucket
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER --windowDurationSeconds 3600 --bucket MONTHS

# 1-day aggregates - no bucket
docker run --label=cassandra-writer --network=host \
  auth-ece/consumer:0.1.0 --applicationType CASSANDRA_WRITER --windowDurationSeconds 86400

# ---- KSQLDB

docker exec -ti ksqldb-cli ksql http://ksqldb-server:8088

# ---- Redis

curl -X POST -H 'Content-Type: application/json' --data @kafka-connect-redis-config.json http://localhost:8083/connectors

docker exec -ti redis redis-cli

SELECT 1

KEYS '*'

GET <GW_ID>

# ---- Clean up

# -- Publisher
# remove all publishers
docker rm --force  $(docker ps --filter ancestor=auth-ece/publisher:0.1.0 -q)
# remove all electricity publishers
docker rm --force  $(docker ps --filter label=electricity-publisher -q)
# remove all  water publishers
docker rm --force  $(docker ps --filter label=water-publisher -q)
# remove all gas publishers
docker rm --force  $(docker ps --filter label=gas-publisher -q)

# -- Consumer
# remove all types of consumer apps
docker rm --force  $(docker ps --filter ancestor=auth-ece/consumer:0.1.0 -q)

# -- Aggregator
# remove all aggregator instances
docker rm --force  $(docker ps --filter label=aggregator -q)

# remove all cassandra writer instances
docker rm --force  $(docker ps --filter label=cassandra-writer -q)

# -- Reset state of kafka-streams application

# https://www.confluent.io/blog/data-reprocessing-with-kafka-streams-resetting-a-streams-application/

docker exec -i kafka kafka-streams-application-reset --application-id metrics-aggregates-60 \
                                      --input-topics metrics \
                                      --bootstrap-servers kafka:9092 \
                                      --zookeeper zookeeper:32181

