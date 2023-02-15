package auth.ece.consumer;

import auth.ece.common.model.Metric;
import auth.ece.common.model.avro.MetricAvro;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.Instant;

public class MetricTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        MetricAvro metric = (MetricAvro) record.value();
        if (metric != null && metric.getTimestamp() != null) {
            return Instant.parse(metric.getTimestamp()).toEpochMilli();
        }
        // fallback to stream time
        return partitionTime;
    }
}

