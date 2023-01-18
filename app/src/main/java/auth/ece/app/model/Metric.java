package auth.ece.app.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Metric {
    private MetricType metricType;
    private Instant timestamp;
    private Double value;
}