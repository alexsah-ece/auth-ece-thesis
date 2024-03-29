package auth.ece.consumer;

import auth.ece.common.model.Metric;
import auth.ece.common.model.MetricAttribute;
import auth.ece.common.model.MetricType;
import auth.ece.common.model.avro.MetricAvro;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;

@Log4j2
public class ConsoleConsumer {
    private KafkaConsumer consumer;

    public ConsoleConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // https://stackoverflow.com/questions/39606026
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        consumer = new KafkaConsumer<String, MetricAvro>(props);
    }

     public void consume(String topic) {
         consumer.subscribe(Arrays.asList(topic));
         try {
             while (true) {
                 ConsumerRecords<String, MetricAvro> records = consumer.poll(100);
                 for (ConsumerRecord<String, MetricAvro> record : records) {
                     log.info("offset = {}, key = {}, value = {}",
                             record.offset(), record.key(), transformToMetric(record.key(), record.value()));
                 }
             }
         } finally {
             consumer.close();
         }
     }

     private Metric transformToMetric(String key, MetricAvro metricAvro) {
         return Metric.builder()
                 .gateway(metricAvro.getGateway())
                 .metricType(MetricType.valueOf(metricAvro.getMetricType()))
                 .timestamp(Instant.parse(metricAvro.getTimestamp()))
                 .metricAttribute(MetricAttribute.valueOf(metricAvro.getMetricAttribute()))
                 .value(metricAvro.getValue())
                 .build();
     }
}
