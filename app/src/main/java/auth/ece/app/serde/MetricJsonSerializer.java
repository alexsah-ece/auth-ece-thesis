package auth.ece.app.serde;


import auth.ece.app.converter.InstantConverter;
import auth.ece.app.model.Metric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MetricJsonSerializer implements Serializer<Metric> {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantConverter())
            .create();

    @Override
    public byte[] serialize(String topic, Metric metric) {
        return gson.toJson(metric).getBytes(StandardCharsets.UTF_8);
    }
}
