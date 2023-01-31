package auth.ece.replay.processor;

import auth.ece.replay.model.DatasetMetric;
import auth.ece.common.model.Metric;

import java.util.List;

public abstract class DatasetProcessor {

    protected int householdId;

    public DatasetProcessor(int householdId) {
        this.householdId = householdId;
    }
    public abstract List<Metric> transform(DatasetMetric metric);
    public int getHouseholdId() {
        return householdId;
    };

    protected double getAdjustedValue(double value, double coEff) {
        return value + (coEff * householdId);
    }

}
