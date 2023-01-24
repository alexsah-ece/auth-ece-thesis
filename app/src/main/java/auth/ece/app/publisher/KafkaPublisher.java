package auth.ece.app.publisher;

import auth.ece.app.model.Metric;
import auth.ece.app.processor.DatasetProcessor;
import auth.ece.app.serde.MetricJsonSerializer;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

@Log4j2
public class KafkaPublisher extends MetricPublisher {

    private KafkaProducer producer;

    public KafkaPublisher(double permitsPerSecond, DatasetProcessor datasetProcessor, Properties kafkaProps) {
        super(permitsPerSecond, datasetProcessor);
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                MetricJsonSerializer.class.getName());
        producer = new KafkaProducer<String, Metric>(kafkaProps);
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
        ProducerRecord<String, Metric> record =
                new ProducerRecord<>("metrics", getRoutingKey(metric), metric);
        producer.send(record, new DemoProducerCallback());
    }

    private String getRoutingKey(Metric metric) {
        /* household - id of the household
        /* metric type - electricity, gas, water
        /* metric attribute - specific to the metric type. For example, for electricity:
        /* voltage, intensity, active power, reactive power
         */
        return String.format("<household>.electricity.%s", metric.getMetricType());
    }
}
