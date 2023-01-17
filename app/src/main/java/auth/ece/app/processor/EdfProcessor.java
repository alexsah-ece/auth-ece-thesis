package auth.ece.app.processor;

import auth.ece.app.model.EdfDataset;
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
public class EdfProcessor {
    public List<Metric> transform(List<EdfDataset> edfDatasetList) {
        return edfDatasetList.stream()
                .map(item -> edfDatasetToMetrics(item))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Metric> edfDatasetToMetrics(EdfDataset edfDataset) {
        Instant timestamp = getTimestamp(edfDataset);
        ArrayList<Metric> metrics = new ArrayList<>();
        metrics.add(getActivePower(timestamp, edfDataset));
        metrics.add(getReactivePower(timestamp, edfDataset));
        metrics.add(getVoltage(timestamp, edfDataset));
        metrics.add(getIntensity(timestamp, edfDataset));
        return metrics;
    }

    private Instant getTimestamp(EdfDataset edfDataset) {
        String stringDate = String.join(",", edfDataset.getDate(), edfDataset.getTime());
        String pattern = "d/M/uuuu,HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, dateTimeFormatter);
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return instant;
    }

    private Metric getActivePower(Instant timestamp, EdfDataset edfDataset) {
        return Metric.builder()
                .metricType(MetricType.ACTIVE_POWER)
                .timestamp(timestamp)
                .value(edfDataset.getReactivePower().doubleValue())
                .build();
    }

    private Metric getReactivePower(Instant timestamp, EdfDataset edfDataset) {
        return Metric.builder()
                .metricType(MetricType.REACTIVE_POWER)
                .timestamp(timestamp)
                .value(edfDataset.getActivePower().doubleValue())
                .build();
    }

    private Metric getVoltage(Instant timestamp, EdfDataset edfDataset) {
        return Metric.builder()
                .metricType(MetricType.VOLTAGE)
                .timestamp(timestamp)
                .value(edfDataset.getVoltage().doubleValue())
                .build();
    }

    private Metric getIntensity(Instant timestamp, EdfDataset edfDataset) {
        return Metric.builder()
                .metricType(MetricType.INTENSITY)
                .timestamp(timestamp)
                .value(edfDataset.getIntensity().doubleValue())
                .build();
    }


}
