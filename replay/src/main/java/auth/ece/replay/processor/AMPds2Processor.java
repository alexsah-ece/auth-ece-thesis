package auth.ece.replay.processor;

import auth.ece.replay.model.*;
import auth.ece.common.model.Metric;
import auth.ece.common.model.MetricAttribute;
import auth.ece.common.model.MetricType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AMPds2Processor extends DatasetProcessor {
    private MetricType metricType;

    public AMPds2Processor(List<Integer> householdIdList, MetricType metricType) {
        super(householdIdList);
        setMetricType(metricType);
    }

    private void setMetricType(MetricType metricType) {
        if (!(metricType.equals(MetricType.GAS) || metricType.equals(MetricType.WATER))) {
            throw new RuntimeException("AMPds2Processor only supports GAS ans WATER metrics");
        } else {
            this.metricType = metricType;
        }
    }

    @Override
    public List<Metric> transform(DatasetMetric aMPds2Metric) {
        return aMPds2ToMetrics((AMPds2Metric) aMPds2Metric);
    }

    private List<Metric> aMPds2ToMetrics(AMPds2Metric metric) {
        Instant timestamp = getTimestamp(metric);
        ArrayList<Metric> metrics = new ArrayList<>();
        for (String id: gatewayIdList) {
            if (metricType.equals(MetricType.WATER)) {
                metrics.add(getWaterIdd(timestamp, metric, id));
                metrics.add(getWaterSummation(timestamp, metric, id));
            } else {
                metrics.add(getGasIdd(timestamp, metric, id));
                metrics.add(getGasSummation(timestamp, metric, id));
            }
        }
        return metrics;
    }

    private Instant getTimestamp(AMPds2Metric metric) {
        return Instant.ofEpochSecond(metric.getUnixTimestamp());
    }

    private Metric getWaterIdd(Instant timestamp, AMPds2Metric metric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.WATER)
                .metricAttribute(MetricAttribute.WATER_IDD)
                .timestamp(timestamp)
                .value(getAdjustedValue(metric.getInstRate().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getWaterSummation(Instant timestamp, AMPds2Metric metric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.WATER)
                .metricAttribute(MetricAttribute.WATER_SUMMATION)
                .timestamp(timestamp)
                .value(getAdjustedValue(metric.getCounter().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getGasIdd(Instant timestamp, AMPds2Metric metric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.GAS)
                .metricAttribute(MetricAttribute.GAS_IDD)
                .timestamp(timestamp)
                .value(getAdjustedValue(metric.getInstRate().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getGasSummation(Instant timestamp, AMPds2Metric metric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.GAS)
                .metricAttribute(MetricAttribute.GAS_SUMMATION)
                .timestamp(timestamp)
                .value(getAdjustedValue(metric.getCounter().doubleValue(), coEff, gatewayId))
                .build();
    }
}
