package auth.ece.persistence.cassandra;

import auth.ece.common.model.AggregateMetric;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Log4j2
public class CassandraDao {

    private final CqlSession session;

    private final String tableName;

    private PreparedStatement insertBucketedPreparedStatement;
    private PreparedStatement insertNonBucketedPreparedStatement;


    public CassandraDao(CqlSession session, String tableName) {
        this.tableName = tableName;
        this.session = session;
        // can't prepare both for bucketed and non-bucketed tables, so one of the two will always fail, depending
        // on the table
        try {
            this.insertBucketedPreparedStatement = this.getPreparedBucketedStatement();
        } catch (InvalidQueryException e) {
            log.warn("Could not send bucketed prepared statement for table " + tableName);
        }
        try {
            this.insertNonBucketedPreparedStatement = this.getPreparedNonBucketedStatement();
        } catch (InvalidQueryException e) {
            log.warn("Could not send non bucketed prepared statement for table " + tableName);
        }


    }

    private PreparedStatement getPreparedBucketedStatement() {
        // not coming from user input, safe from SQL injection
        String insertBucketed = new StringBuilder(String.format("insert into metrics.%s ", tableName))
                .append("(gateway, metric, attribute, bucket, windowStart, min, max, avg, sampleCount) ")
                .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .toString();
        return session.prepare(insertBucketed);
    }

    private PreparedStatement getPreparedNonBucketedStatement() {
        // not coming from user input, safe from SQL injection
        String insertNonBucketed = new StringBuilder(String.format("insert into metrics.%s ", tableName))
                .append("(gateway, metric, attribute, windowStart, min, max, avg, sampleCount) ")
                .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                .toString();
        return session.prepare(insertNonBucketed);
    }


    private void insertBucketedMetric(ChronoUnit bucket, String gateway, AggregateMetric metric) {
        BoundStatement boundStatement = this.insertBucketedPreparedStatement.boundStatementBuilder()
                .setString(0, gateway)
                .setString(1, metric.getMetricType().toString())
                .setString(2, metric.getMetricAttribute().toString())
                .setInstant(3, truncateTimestamp(metric.getTimestamp(), bucket))
                .setInstant(4, metric.getTimestamp())
                .setDouble(5, metric.getMin())
                .setDouble(6, metric.getMax())
                .setDouble(7, metric.getAvg())
                .setLong(8, metric.getSampleCount())
                .build();
        session.execute(boundStatement);
    }

    public void insertMinutelyBucketedMetric(String gateway, AggregateMetric metric) {
        this.insertBucketedMetric(ChronoUnit.MINUTES, gateway, metric);
    }

    public void insertHourlyBucketedMetric(String gateway, AggregateMetric metric) {
        this.insertBucketedMetric(ChronoUnit.HOURS, gateway, metric);
    }

    public void insertDailyBucketedMetric(String gateway, AggregateMetric metric) {
        this.insertBucketedMetric(ChronoUnit.DAYS, gateway, metric);
    }

    public void insertMonthlyBucketedMetric(String gateway, AggregateMetric metric) {
        this.insertBucketedMetric(ChronoUnit.MONTHS, gateway, metric);
    }

    public void insertNonBucketedMetric(String gateway, AggregateMetric metric) {
        BoundStatement boundStatement = this.insertNonBucketedPreparedStatement.boundStatementBuilder()
                .setString(0, gateway)
                .setString(1, metric.getMetricType().toString())
                .setString(2, metric.getMetricAttribute().toString())
                .setInstant(3, metric.getTimestamp())
                .setDouble(4, metric.getMin())
                .setDouble(5, metric.getMax())
                .setDouble(6, metric.getAvg())
                .setLong(7, metric.getSampleCount())
                .build();
        session.execute(boundStatement);
    }

    private Instant truncateTimestamp(Instant timestamp, ChronoUnit unit) {
        Instant truncatedDate = null;
        switch (unit) {
            case MINUTES:
            case HOURS:
            case DAYS:
                truncatedDate = timestamp.truncatedTo(unit);
                break;
            case MONTHS:
                truncatedDate = YearMonth.from(timestamp.atZone(ZoneId.of("UTC"))).atDay(1)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC);
                break;
        }
        return truncatedDate;
    }
}
