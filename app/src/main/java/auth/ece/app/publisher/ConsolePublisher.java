package auth.ece.app.publisher;

import auth.ece.app.model.Metric;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ConsolePublisher {

    RateLimiter rateLimiter;
    public ConsolePublisher(double permitsPerSecond) {
        rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    public void publishMetrics(List<Metric> metricList) {
        log.info(String.format("About to publish: %d metrics", metricList.size()));
        for(Metric metric: metricList) {
            publishMetric(metric);
        }
    }

    public void publishMetric(Metric metric) {
        rateLimiter.acquire();
        log.info(metric);
    }
}
