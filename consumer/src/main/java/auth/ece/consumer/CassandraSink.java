package auth.ece.consumer;

import auth.ece.common.model.AggregateMetric;
import auth.ece.consumer.serialization.CustomAggregateDeserializer;
import auth.ece.persistence.cassandra.CassandraDao;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import java.util.Properties;

@Log4j2
public class CassandraSink {

    private final long windowDurationSeconds;
    private KafkaConsumer consumer;
    private final ChronoUnit bucket;

    private final String sourceTopic;

    private final CassandraDao cassandraDao;

    public CassandraSink(CassandraDao cassandraDao, ChronoUnit bucket, long windowDurationSeconds) {
        this.windowDurationSeconds = windowDurationSeconds;
        this.cassandraDao = cassandraDao;
        this.bucket = bucket;

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
        try {
            while (true) {
                ConsumerRecords<String, CustomAggregate> records = consumer.poll(100);
                for (ConsumerRecord<String, CustomAggregate> record : records) {
                    var customAggregate = record.value();
                    var aggMetric = transformToAggMetric(customAggregate);
                    log.info("offset = {}, key = {}, value = {}",
                            record.offset(), record.key(), aggMetric);
                    insertToCassandra(customAggregate.getGateway(), aggMetric);
                }
            }
        } finally {
            consumer.close();
        }
    }

    private void insertToCassandra(String gateway, AggregateMetric metric) {
        if (bucket == null) {
            cassandraDao.insertNonBucketedMetric(gateway, metric);
        } else if (bucket.equals(ChronoUnit.MINUTES)) {
            cassandraDao.insertMinutelyBucketedMetric(gateway, metric);
        } else if (bucket.equals(ChronoUnit.HOURS)) {
            cassandraDao.insertHourlyBucketedMetric(gateway, metric);
        } else if (bucket.equals(ChronoUnit.DAYS)){
            cassandraDao.insertDailyBucketedMetric(gateway, metric);
        } else if (bucket.equals(ChronoUnit.MONTHS)) {
            cassandraDao.insertMonthlyBucketedMetric(gateway, metric);
        }
    }

    private AggregateMetric transformToAggMetric(CustomAggregate customAggregate) {
        return AggregateMetric.builder()
                .metricType(customAggregate.getMetricType())
                .metricAttribute(customAggregate.getMetricAttribute())
                .timestamp(customAggregate.getWindowStart())
                .max(customAggregate.getMax())
                .min(customAggregate.getMin())
                .avg(customAggregate.getAverage())
                .sampleCount(customAggregate.getSampleCount())
                .build();
    }

    private String getAppId() {
        return String.format("cassandra-writer-%s", this.sourceTopic);
    }

    public static String getTargetTableName(long windowDurationSeconds) {
        return String.format("metrics_aggregates_%d", windowDurationSeconds);
    }
}
