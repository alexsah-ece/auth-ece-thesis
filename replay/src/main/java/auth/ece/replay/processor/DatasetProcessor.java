package auth.ece.replay.processor;

import auth.ece.replay.model.DatasetMetric;
import auth.ece.common.model.Metric;

import java.util.List;

public abstract class DatasetProcessor {

    protected String gatewayId;

    public DatasetProcessor(int gatewayId) {
        this.gatewayId = Integer.toString(gatewayId);
    }
    public abstract List<Metric> transform(DatasetMetric metric);
    public String getGatewayId() {
        return gatewayId;
    }

    protected double getAdjustedValue(double value, double coEff) {
        return value + (coEff * Integer.parseInt(gatewayId));
    }

}
