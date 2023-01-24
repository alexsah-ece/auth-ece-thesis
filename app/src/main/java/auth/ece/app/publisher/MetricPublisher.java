package auth.ece.app.publisher;

import auth.ece.app.model.DatasetMetric;
import auth.ece.app.model.Metric;
import auth.ece.app.processor.DatasetProcessor;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.log4j.Log4j2;

import java.util.Iterator;
import java.util.List;

@Log4j2
public abstract class MetricPublisher {
    protected RateLimiter rateLimiter;
    protected DatasetProcessor processor;

    public MetricPublisher(double permitsPerSecond, DatasetProcessor datasetProcessor) {
        rateLimiter = RateLimiter.create(permitsPerSecond);
        processor = datasetProcessor;
    }

    public void publishMetrics(Iterator<DatasetMetric> metricIterator) {
        while (metricIterator.hasNext()) {
            DatasetMetric datasetMetric = metricIterator.next();
            List<Metric> metricList = processor.transform(datasetMetric);
            for(Metric metric: metricList) {
                publishMetric(metric);
            }
        }
        log.info("Exiting: " + metricIterator.hasNext());
    }

    public void publishMetric(Metric metric) {
        rateLimiter.acquire();
        publish(metric);
    }

    abstract void publish(Metric metric);
}
