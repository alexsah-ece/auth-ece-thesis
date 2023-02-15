package auth.ece.consumer;

import auth.ece.common.model.MetricAttribute;
import auth.ece.common.model.MetricType;
import auth.ece.common.model.avro.MetricAvro;
import lombok.ToString;

import java.time.Instant;

@ToString
public class CustomAggregate {

    private Instant windowStart;
    private Instant windowEnd;
    private long sampleCount;
    private double min;
    private double max;
    private double sum;
    private double average;
    private MetricType metricType;
    private MetricAttribute metricAttribute;
    private String gateway;

    public CustomAggregate() {
        sampleCount = 0;
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sum = 0;
    }

    public CustomAggregate addMetric(MetricAvro metricAvro) {
        double value = metricAvro.getValue();
        sampleCount += 1;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
        return this;
    }

    public void setAverage() {
        average = sum / sampleCount;
    }

    public void setWindowStart(Instant windowStart) {
        this.windowStart = windowStart;
    }

    public void setWindowEnd(Instant windowEnd) {
        this.windowEnd = windowEnd;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public void setMetricAttribute(MetricAttribute metricAttribute) {
        this.metricAttribute = metricAttribute;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setAttributesFromKey(String key) {
        String[] split = key.split("\\.");
        setGateway(split[0]);
        setMetricType(MetricType.valueOf(split[1]));
        setMetricAttribute(MetricAttribute.valueOf(split[2]));
    }
}
