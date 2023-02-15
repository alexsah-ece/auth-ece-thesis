package auth.ece.consumer;

import auth.ece.common.model.avro.MetricAvro;
import auth.ece.consumer.serialization.JsonSerdes;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import java.util.Properties;

@Log4j2
public class MetricsAggregator {

    private static final int GRACE_PERIOD_SEC = 10;

    private final long windowDurationSeconds;
    private final String sourceTopic;
    private final Properties props;

    private final int clientId;


    public MetricsAggregator(long windowDurationSeconds, String sourceTopic, int clientId) {
        this.windowDurationSeconds = windowDurationSeconds;
        this.sourceTopic = sourceTopic;
        this.clientId = clientId;

        // we allow the following system properties to be overridden
        String host = "localhost";
        Integer port = getPort();
        String stateDir = getStateDir();
        String endpoint = String.format("%s:%s", host, port);

        // set the required properties for running Kafka Streams
        props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, getTargetTopic());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
    }

    private Integer getPort() {
        return 7000 + clientId;
    }

    private String getStateDir() {
        return String.format("/tmp/kafka-streams-%d", clientId);
    }

    public void start() {
        Topology topology = getTopology();
        KafkaStreams streams = new KafkaStreams(topology, props);
        // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.cleanUp();
        streams.start();
    }

    public Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // configure consumption from metrics stream

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081");
        final Serde<MetricAvro> valueSerde = new SpecificAvroSerde<>();
        valueSerde.configure(serdeConfig, false); // `false` for record values

        Consumed<String, MetricAvro> metricConsumerOptions =
                Consumed.with(Serdes.String(), valueSerde)
                        .withTimestampExtractor(new MetricTimestampExtractor());

        KStream<String, MetricAvro> avroMetricsStream = builder.stream(sourceTopic, metricConsumerOptions);

        // setup windowed aggregation
        TimeWindows timeWindow = TimeWindows
                .ofSizeAndGrace(Duration.ofSeconds(windowDurationSeconds), Duration.ofSeconds(GRACE_PERIOD_SEC));

        Initializer<CustomAggregate> initializer = CustomAggregate::new;
        Aggregator<String, MetricAvro, CustomAggregate> aggregator = ((key, value, aggregate) -> aggregate.addMetric(value));

        KTable<Windowed<String>, CustomAggregate> aggregates = avroMetricsStream
                .groupByKey()
                .windowedBy(timeWindow)
                        .aggregate(
                                initializer,
                                aggregator,
                                Materialized.<String, CustomAggregate, WindowStore<Bytes, byte[]>>as(getTargetTopic())
                                        .withKeySerde(Serdes.String())
                                        .withValueSerde(JsonSerdes.CustomAggregate())
                        )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()));

        // complete aggregation and publish results to relevant destination
        aggregates.toStream()
                .map((key, value) -> {
                    value.setAverage();
                    value.setAttributesFromKey(key.key());
                    value.setWindowStart(key.window().startTime());
                    value.setWindowEnd(key.window().endTime());
                    return KeyValue.pair(key.key(), value);
                })
                .to(getTargetTopic(), Produced.with(Serdes.String(), JsonSerdes.CustomAggregate()));
                //.print(Printed.<Windowed<String>, CustomAggregate>toSysOut().withLabel(getTargetTopic()));

        return builder.build();
    }

    private String getTargetTopic() {
        return String.format("metrics-aggregates-%d", windowDurationSeconds);
    }
}
