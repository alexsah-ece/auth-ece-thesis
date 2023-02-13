package auth.ece.common.serialization;

import auth.ece.common.converter.InstantConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {
    private Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(Instant.class, new InstantConverter())
                    .create();

    /** Default constructor needed by Kafka */
    public JsonSerializer() {}

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, T type) {
        return gson.toJson(type).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {}
}
