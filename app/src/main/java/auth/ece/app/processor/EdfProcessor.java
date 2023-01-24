package auth.ece.app.processor;

import auth.ece.app.model.DatasetMetric;
import auth.ece.app.model.EdfMetric;
import auth.ece.app.model.Metric;
import auth.ece.app.model.MetricType;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
public class EdfProcessor implements DatasetProcessor {

    private int householdId;

    public EdfProcessor(int householdId) {
        this.householdId = householdId;
    }

    public int getHouseholdId() {
        return householdId;
    }

    public List<Metric> transform(List<EdfMetric> edfMetricList) {
        return edfMetricList.stream()
                .map(item -> transform(item))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Metric> transform(DatasetMetric edfMetric) {
        return edfDatasetToMetrics((EdfMetric) edfMetric);
    }

    private List<Metric> edfDatasetToMetrics(EdfMetric edfMetric) {
        Instant timestamp = getTimestamp(edfMetric);
        ArrayList<Metric> metrics = new ArrayList<>();
        metrics.add(getActivePower(timestamp, edfMetric));
        metrics.add(getReactivePower(timestamp, edfMetric));
        metrics.add(getVoltage(timestamp, edfMetric));
        metrics.add(getIntensity(timestamp, edfMetric));
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

    private Metric getActivePower(Instant timestamp, EdfMetric edfMetric) {
        double coEff = 0.001;
        return Metric.builder()
                .metricType(MetricType.ACTIVE_POWER)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getReactivePower().doubleValue(), coEff))
                .build();
    }

    private Metric getReactivePower(Instant timestamp, EdfMetric edfMetric) {
        double coEff = 0.001;
        return Metric.builder()
                .metricType(MetricType.REACTIVE_POWER)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getActivePower().doubleValue(), coEff))
                .build();
    }

    private Metric getVoltage(Instant timestamp, EdfMetric edfMetric) {
        double coEff = 0.001;
        return Metric.builder()
                .metricType(MetricType.VOLTAGE)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getVoltage().doubleValue(), coEff))
                .build();
    }

    private Metric getIntensity(Instant timestamp, EdfMetric edfMetric) {
        double coEff = 0.001;
        return Metric.builder()
                .metricType(MetricType.INTENSITY)
                .timestamp(timestamp)
                .value(getAdjustedValue(edfMetric.getIntensity().doubleValue(), coEff))
                .build();
    }

    private double getAdjustedValue(double value, double coEff) {
        return value + (coEff * householdId);
    }

}
