package auth.ece.consumer.serialization;

import auth.ece.common.model.Metric;
import auth.ece.common.serialization.JsonDeserializer;
import auth.ece.common.serialization.JsonSerializer;
import auth.ece.consumer.CustomAggregate;
import com.datastax.oss.driver.api.core.metadata.schema.Describable;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {
    public static Serde<CustomAggregate> CustomAggregate() {
        JsonSerializer<CustomAggregate> serializer = new JsonSerializer<>();
        JsonDeserializer<CustomAggregate> deserializer = new JsonDeserializer<>(CustomAggregate.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Metric> Enriched() {
        JsonSerializer<Metric> serializer = new JsonSerializer<>();
        JsonDeserializer<Metric> deserializer = new JsonDeserializer<>(Metric.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}
