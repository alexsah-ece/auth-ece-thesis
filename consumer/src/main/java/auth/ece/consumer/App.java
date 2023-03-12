package auth.ece.consumer;

import auth.ece.persistence.cassandra.CassandraDao;
import com.datastax.oss.driver.api.core.CqlSession;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.checkerframework.checker.units.qual.A;

import java.time.temporal.ChronoUnit;

@Log4j2
public class App {

    private static final String SOURCE_TOPIC_OPTION = "sourceTopic";
    private static final String WINDOW_DURATION_SECONDS_OPTION = "windowDurationSeconds";

    private static final String DESIRED_MESSAGE_COUNT_OPTION = "desiredMessageCount";

    private static final String CLIENT_ID_OPTION = "clientId";

    private static final String BUCKET_OPTION = "bucket";

    private static final String APPLICATION_TYPE = "applicationType";

    enum ApplicationType {
        AGGREGATOR,
        CONSOLE_CONSUMER,
        CASSANDRA_WRITER,
        AGGREGATES_PERFORMANCE_TRACKER
    }
    public static void main(String[] args) {
        CommandLine cli = parseArgs(args);
        String sourceTopic = cli.getOptionValue(SOURCE_TOPIC_OPTION);
        long windowDurationSeconds;
        try {
            windowDurationSeconds = Long.parseLong(cli.getOptionValue(WINDOW_DURATION_SECONDS_OPTION));
        } catch (Exception e) {
            log.error("Defaulting to 60 for window duration, as could not read CLI input");
            windowDurationSeconds = 60;
        }
        int clientId;
        try {
            clientId = Integer.parseInt(cli.getOptionValue(CLIENT_ID_OPTION));
        } catch (Exception e) {
            log.error("Defaulting to 1 for client id, as could not read CLI input");
            clientId = 1;
        }
        ChronoUnit bucket = null;
        try {
            bucket = ChronoUnit.valueOf(cli.getOptionValue(BUCKET_OPTION));
        } catch (Exception e) {
            log.warn("Was not able to parse any bucket option as CHRONO_UNIT, defaulting to null");
        }
        ApplicationType applicationType = null;
        try {
            applicationType = ApplicationType.valueOf(cli.getOptionValue(APPLICATION_TYPE));
        } catch (Exception e) {
            log.warn("Was not able to parse any application type option, defaulting to CONSOLE_CONSUMER");
            applicationType = ApplicationType.CONSOLE_CONSUMER;
        }
        long desiredMessageCount = 0;
        try {
            desiredMessageCount = Long.parseLong(cli.getOptionValue(DESIRED_MESSAGE_COUNT_OPTION));
        } catch (Exception e) {
            log.warn("Was not able to parse any desiredMessageCount option, defaulting to 0");
        }
        if (applicationType.equals(ApplicationType.AGGREGATOR)) {
            MetricsAggregator aggregator = new MetricsAggregator(windowDurationSeconds, sourceTopic, clientId);
            aggregator.start();
        } else if (applicationType.equals(ApplicationType.CASSANDRA_WRITER)) {
            runCassandraSink(windowDurationSeconds, bucket, desiredMessageCount);
        } else if (applicationType.equals(ApplicationType.AGGREGATES_PERFORMANCE_TRACKER)) {
            AggregatesPerformanceTracker tracker = new AggregatesPerformanceTracker(windowDurationSeconds, desiredMessageCount);
            tracker.consume();
        } else {
            ConsoleConsumer consumer = new ConsoleConsumer();
            executeConsumer(consumer);
        }
    }

    public static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option applicationType = new Option("t", APPLICATION_TYPE, true, "Application to run. Can" +
                " be one of AGGREGATOR, CONSOLE_CONSUMER, CASSANDRA_WRITER");

        Option sourceTopic = new Option("s", SOURCE_TOPIC_OPTION, true, "Source topic");

        Option windowDurationSeconds = new Option("w", WINDOW_DURATION_SECONDS_OPTION, true,
                "Aggregation window duration in seconds");

        Option clientId = new Option("c", CLIENT_ID_OPTION, true, "Client id to differentiate" +
                " Kafka Streams apps. Make sure that this is unique per process for processes running in the same host");

        Option bucket = new Option("b", BUCKET_OPTION, true, "Bucket to group metrics by when writing" +
                " to cassandra");

        Option desiredMessageCount = new Option("d", DESIRED_MESSAGE_COUNT_OPTION, true,
                "Desired message count for tracking aggregator performance");

        options.addOption(applicationType);
        options.addOption(sourceTopic);
        options.addOption(windowDurationSeconds);
        options.addOption(clientId);
        options.addOption(bucket);
        options.addOption(desiredMessageCount);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Available options", options);
            System.exit(1);
        }
        return null;
    }

    public static void executeConsumer(ConsoleConsumer consumer) {
        consumer.consume("metrics");
    }

    public static void runCassandraSink(long windowDurationSeconds, ChronoUnit bucket, long count) {
        try (CqlSession session = CqlSession.builder().build()) {
            String tableName = CassandraSink.getTargetTableName(windowDurationSeconds);
            CassandraDao dao = new CassandraDao(session, tableName);
            CassandraSink sink = new CassandraSink(dao, bucket, windowDurationSeconds, count);
            sink.consume();
        }
    }
}
