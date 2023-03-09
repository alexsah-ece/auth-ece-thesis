package auth.ece.replay.processor;

import auth.ece.replay.model.*;
import auth.ece.common.model.Metric;
import auth.ece.common.model.MetricAttribute;
import auth.ece.common.model.MetricType;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
public class EdfProcessor extends DatasetProcessor {

    public EdfProcessor(List<Integer> householdIdList) {
        super(householdIdList);
    }

    @Override
    public List<Metric> transform(DatasetMetric edfMetric) {
        return edfDatasetToMetrics((EdfMetric) edfMetric);
    }

    private List<Metric> edfDatasetToMetrics(EdfMetric edfMetric) {
        Instant timestamp = getTimestamp(edfMetric);
        ArrayList<Metric> metrics = new ArrayList<>();
        for(String id: gatewayIdList) {
            metrics.add(getActivePower(timestamp, edfMetric, id));
            metrics.add(getReactivePower(timestamp, edfMetric, id));
            metrics.add(getVoltage(timestamp, edfMetric, id));
            metrics.add(getIntensity(timestamp, edfMetric, id));
        }
        return metrics;
    }

    private Instant getTimestamp(EdfMetric edfMetric) {
        String stringDate = String.join(",", edfMetric.getDate(), edfMetric.getTime());
        String pattern = "d/M/uuuu,HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, dateTimeFormatter);
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return instant;
    }

    private Metric getActivePower(Instant timestamp, EdfMetric edfMetric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.ELECTRICITY)
                .metricAttribute(MetricAttribute.ACTIVE_POWER)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getReactivePower().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getReactivePower(Instant timestamp, EdfMetric edfMetric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.ELECTRICITY)
                .metricAttribute(MetricAttribute.REACTIVE_POWER)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getActivePower().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getVoltage(Instant timestamp, EdfMetric edfMetric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.ELECTRICITY)
                .metricAttribute(MetricAttribute.VOLTAGE)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getVoltage().doubleValue(), coEff, gatewayId))
                .build();
    }

    private Metric getIntensity(Instant timestamp, EdfMetric edfMetric, String gatewayId) {
        double coEff = 0.001;
        return Metric.builder()
                .gateway(gatewayId)
                .metricType(MetricType.ELECTRICITY)
                .metricAttribute(MetricAttribute.INTENSITY)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getIntensity().doubleValue(), coEff, gatewayId))
                .build();
    }
}
