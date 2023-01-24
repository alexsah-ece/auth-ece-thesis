package auth.ece.app.publisher;

import auth.ece.app.model.Metric;
import auth.ece.app.processor.DatasetProcessor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConsolePublisher extends MetricPublisher {

    public ConsolePublisher(double permitsPerSecond, DatasetProcessor datasetProcessor) {
        super(permitsPerSecond, datasetProcessor);
    }

    @Override
    void publish(Metric metric) {
        log.info(metric);
    }
}
