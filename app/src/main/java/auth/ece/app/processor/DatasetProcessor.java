package auth.ece.app.processor;

import auth.ece.app.model.DatasetMetric;
import auth.ece.app.model.Metric;

import java.util.List;

public interface DatasetProcessor {
    List<Metric> transform(DatasetMetric metric);
}
