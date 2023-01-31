package auth.ece.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Metric {
    private MetricType metricType;
    private MetricAttribute metricAttribute;
    private Instant timestamp;
    private Double value;
}
