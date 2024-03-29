package auth.ece.replay.publisher;

import auth.ece.replay.processor.DatasetProcessor;
import auth.ece.common.model.Metric;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConsolePublisher extends MetricPublisher {

    public ConsolePublisher(long count, double permitsPerSecond, DatasetProcessor datasetProcessor) {
        super(count, permitsPerSecond, datasetProcessor);
    }

    @Override
    void publish(Metric metric) {
        log.info(metric);
    }
}
