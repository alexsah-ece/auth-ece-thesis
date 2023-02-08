package auth.ece.persistence.cassandra;

import auth.ece.common.model.AggregateMetric;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.time.temporal.ChronoUnit;

public class CassandraDao {

    private final CqlSession session;

    private final PreparedStatement insertMinutelyPreparedStatement;
    private final PreparedStatement insertHourlyPreparedStatement;
    private final PreparedStatement insertMonthlyPreparedStatement;

    public CassandraDao(CqlSession session) {
        this.session = session;
        this.insertMinutelyPreparedStatement = getInsertMinutelyPreparedStatement();
        this.insertHourlyPreparedStatement = getInsertHourlyPreparedStatement();
        this.insertMonthlyPreparedStatement = getInsertMonthlyPreparedStatement();
    }

    private PreparedStatement getInsertMinutelyPreparedStatement() {
        String insertMinutelyQuery = new StringBuilder("insert into metrics.metrics_minutely ")
                .append("(gateway, metric, attribute, bucket, minute, min, max, avg) ")
                .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                .toString();
        return session.prepare(insertMinutelyQuery);
    }

    private PreparedStatement getInsertHourlyPreparedStatement() {
        String insertHourlyQuery = new StringBuilder("insert into metrics.metrics_hourly ")
                .append("(gateway, metric, attribute, bucket, hour, min, max, avg) ")
                .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                .toString();
        return session.prepare(insertHourlyQuery);
    }

    private PreparedStatement getInsertMonthlyPreparedStatement() {
        String insertMonthlyQuery = new StringBuilder("insert into metrics.metrics_monthly ")
                .append("(gateway, metric, attribute, month, min, max, avg) ")
                .append("VALUES (?, ?, ?, ?, ?, ?, ?)")
                .toString();
        return session.prepare(insertMonthlyQuery);
    }

    public void insertMinutelyMetric(String gateway, AggregateMetric metric) {
        BoundStatement boundStatement = this.insertMinutelyPreparedStatement.boundStatementBuilder()
                .setString(0, gateway)
                .setString(1, metric.getMetricType().toString())
                .setString(2, metric.getMetricAttribute().toString())
                .setInstant(3, metric.getTimestamp().truncatedTo(ChronoUnit.DAYS))
                .setInstant(4, metric.getTimestamp())
                .setDouble(5, metric.getMin())
                .setDouble(6, metric.getMax())
                .setDouble(7, metric.getAvg())
                .build();
        session.execute(boundStatement);
    }

    public void insertHourlyMetric(String gateway, AggregateMetric metric) {
        BoundStatement boundStatement = this.insertHourlyPreparedStatement.boundStatementBuilder()
                .setString(0, gateway)
                .setString(1, metric.getMetricType().toString())
                .setString(2, metric.getMetricAttribute().toString())
                .setInstant(3, metric.getTimestamp().truncatedTo(ChronoUnit.MONTHS))
                .setInstant(4, metric.getTimestamp())
                .setDouble(5, metric.getMin())
                .setDouble(6, metric.getMax())
                .setDouble(7, metric.getAvg())
                .build();
        session.execute(boundStatement);
    }

    public void insertMonthlyMetric(String gateway, AggregateMetric metric) {
        BoundStatement boundStatement = this.insertMonthlyPreparedStatement.boundStatementBuilder()
                .setString(0, gateway)
                .setString(1, metric.getMetricType().toString())
                .setString(2, metric.getMetricAttribute().toString())
                .setInstant(3, metric.getTimestamp())
                .setDouble(4, metric.getMin())
                .setDouble(5, metric.getMax())
                .setDouble(6, metric.getAvg())
                .build();
        session.execute(boundStatement);
    }
}
