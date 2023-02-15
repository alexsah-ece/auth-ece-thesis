package auth.ece.consumer.serialization;

import auth.ece.common.converter.InstantConverter;
import auth.ece.consumer.CustomAggregate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class CustomAggregateDeserializer implements Deserializer<CustomAggregate> {
    private Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(Instant.class, new InstantConverter())
                    .create();

    @Override
    public CustomAggregate deserialize(String topic, byte[] bytes) {
        if (bytes == null) return null;
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), CustomAggregate.class);
    }

}
