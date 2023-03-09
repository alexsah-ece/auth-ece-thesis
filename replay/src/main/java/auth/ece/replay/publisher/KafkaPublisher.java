package auth.ece.replay.publisher;

import auth.ece.common.model.avro.MetricAvro;
import auth.ece.replay.processor.DatasetProcessor;
import auth.ece.common.model.Metric;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Log4j2
public class KafkaPublisher extends MetricPublisher {

    private KafkaProducer producer;

    public KafkaPublisher(long count, double permitsPerSecond, DatasetProcessor datasetProcessor, Properties kafkaProps) {
        super(count, permitsPerSecond, datasetProcessor);
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        kafkaProps.put("schema.registry.url", "http://localhost:8081");
        producer = new KafkaProducer<String, MetricAvro>(kafkaProps);
    }

    private class DemoProducerCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void publish(Metric metric) {
        log.info("Publishing " + metric);
        ProducerRecord<String, MetricAvro> record =
                new ProducerRecord<>("metrics", getRoutingKey(metric), transformToMetricAvro(metric));
        producer.send(record, new DemoProducerCallback());
    }

    private String getRoutingKey(Metric metric) {
        /* household - id of the household
        /* metric type - electricity, gas, water
        /* metric attribute - specific to the metric type. For example, for electricity:
        /* voltage, intensity, active power, reactive power
         */
        return String.format("%s.%s.%s", processor.getGatewayId(), metric.getMetricType(), metric.getMetricAttribute());
    }

    private MetricAvro transformToMetricAvro(Metric metric) {
        return MetricAvro.newBuilder()
                .setGateway(metric.getGateway())
                .setMetricType(metric.getMetricType().toString())
                .setMetricAttribute(metric.getMetricAttribute().toString())
                .setTimestamp(metric.getTimestamp().toString())
                .setValue(metric.getValue())
                .build();
    }
}
