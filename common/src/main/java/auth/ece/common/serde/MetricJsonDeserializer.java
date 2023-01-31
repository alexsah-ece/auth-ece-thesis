package auth.ece.common.serde;

import auth.ece.common.converter.InstantConverter;
import auth.ece.common.model.Metric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Instant;

public class MetricJsonDeserializer implements Deserializer<Metric> {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantConverter())
            .create();

    @Override
    public Metric deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return gson.fromJson(new String(bytes), Metric.class);
    }
}
