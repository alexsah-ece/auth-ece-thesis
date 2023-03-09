package auth.ece.replay.processor;

import auth.ece.replay.model.DatasetMetric;
import auth.ece.common.model.Metric;

import java.util.List;
import java.util.stream.Collectors;

public abstract class DatasetProcessor {

    protected List<String> gatewayIdList;

    public DatasetProcessor(List<Integer> gatewayIdList) {
        this.gatewayIdList = gatewayIdList.stream()
                .map(gwId -> Integer.toString(gwId))
                .collect(Collectors.toList());
    }
    public abstract List<Metric> transform(DatasetMetric metric);

    protected double getAdjustedValue(double value, double coEff, String gwId) {
        return value + (coEff * Integer.parseInt(gwId));
    }

}
