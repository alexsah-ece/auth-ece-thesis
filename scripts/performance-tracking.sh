
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 100000 -d 0 -s 1 -e 10000 -t water -c 100000

# publish desired message count + publish a few future timestamps to trigger closing of the aggregation window
docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 1000 -t water -c 20000 && docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
    -r 100000 -d 2 -s 1 -e 1000 -t water -c 100

docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 1000 -t water -c 25000


docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
  -r 1000000 -d 0 -s 1 -e 1000 -t water -c 240000 && docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
    -r 100000 -d 2 -s 1 -e 1000 -t water -c 1000


    docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
      -r 1000000 -d 0 -s 1 -e 500 -t water -c 125000

    docker run --label=water-publisher --label=ece-publisher --network=host auth-ece/publisher:0.2.0 \
      -r 1000000 -d 0 -s 501 -e 1000 -t water -c 125000
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


# ----- AggregatesPerformanceTracker

# 1-minute aggregates
docker run --label=performance-tracker --label=performance-tracker-3600 --network=host \
 auth-ece/consumer:0.1.0 --applicationType AGGREGATES_PERFORMANCE_TRACKER \
 --desiredMessageCount 4000 --windowDurationSeconds 3600


 # -- Reset state of kafka-streams application

 # https://www.confluent.io/blog/data-reprocessing-with-kafka-streams-resetting-a-streams-application/

 docker exec -i kafka kafka-streams-application-reset --application-id metrics-aggregates-3600 \
                                       --input-topics metrics \
                                       --bootstrap-servers kafka:9092 \
                                       --zookeeper zookeeper:32181 \
                                       --force