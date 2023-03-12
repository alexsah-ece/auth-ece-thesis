package auth.ece.consumer;

import auth.ece.consumer.serialization.CustomAggregateDeserializer;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;

@Log4j2
public class AggregatesPerformanceTracker {

    private final long desiredMessageCount;
    private final long windowDurationSeconds;
    private final String sourceTopic;

    private KafkaConsumer consumer;

    public AggregatesPerformanceTracker(long windowDurationSeconds, long desiredMessageCount) {
        this.desiredMessageCount = desiredMessageCount;
        this.windowDurationSeconds = windowDurationSeconds;
        this.sourceTopic = MetricsAggregator.getTargetTopic(windowDurationSeconds);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getAppId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomAggregateDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<String, CustomAggregate>(props);

    }

    public void consume() {
        consumer.subscribe(Arrays.asList(sourceTopic));
        long count = 0;
        long firstMessageReceivedAt = 0;
        long desiredCountReachedAt;
        long consumerStartedAt = System.currentTimeMillis();
        log.info(String.format("Consumer started at %d", consumerStartedAt));
        try {
            while (count < desiredMessageCount) {
                ConsumerRecords<String, CustomAggregate> records = consumer.poll(100);
                for (ConsumerRecord<String, CustomAggregate> record : records) {
                    count += 1;
                    if (count == 1) {
                        firstMessageReceivedAt = System.currentTimeMillis();
                        log.info(String.format("First message received at %d", firstMessageReceivedAt));
                    }
                    if (count >= desiredMessageCount) {
                        desiredCountReachedAt = System.currentTimeMillis();
                        log.info(String.format("Received messages (%d) bigger or equal compared to desired %d",
                                count, desiredMessageCount)
                        );
                        log.info(String.format("Desired count reached at %d", desiredCountReachedAt));
                        log.info(String.format("Millis since consumer start: %d",
                                desiredCountReachedAt - consumerStartedAt)
                        );
                        log.info(String.format("Millis since first message received: %d",
                                desiredCountReachedAt - firstMessageReceivedAt)
                        );
                        break;
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

    private String getAppId() {
        return String.format("aggregates-%s-perf-tracking", this.sourceTopic);
    }
}
