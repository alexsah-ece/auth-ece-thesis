package auth.ece.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AggregateMetric {
    private MetricType metricType;
    private MetricAttribute metricAttribute;
    private Instant timestamp;
    private Double avg;
    private Double max;
    private Double min;
    private long sampleCount;
}
