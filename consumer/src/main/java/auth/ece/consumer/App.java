package auth.ece.consumer;

import auth.ece.persistence.cassandra.CassandraDao;
import com.datastax.oss.driver.api.core.CqlSession;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;

import java.time.temporal.ChronoUnit;

@Log4j2
public class App {

    private static final String SOURCE_TOPIC_OPTION = "sourceTopic";
    private static final String WINDOW_DURATION_SECONDS_OPTION = "windowDurationSeconds";

    private static final String CLIENT_ID_OPTION = "clientId";

    private static final String BUCKET_OPTION = "bucket";

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
//        ConsoleConsumer consumer = new ConsoleConsumer();
//        executeConsumer(consumer);
//        MetricsAggregator aggregator = new MetricsAggregator(windowDurationSeconds, sourceTopic, clientId);
//        aggregator.start();
        runCassandraSink(windowDurationSeconds, bucket);
    }

    public static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        Option sourceTopic = new Option("s", SOURCE_TOPIC_OPTION, true, "Source topic");

        Option windowDurationSeconds = new Option("w", WINDOW_DURATION_SECONDS_OPTION, true,
                "Aggregation window duration in seconds");

        Option clientId = new Option("c", CLIENT_ID_OPTION, true, "Client id to differentiate" +
                " Kafka Streams apps. Make sure that this is unique per process for processes running in the same host");

        Option bucket = new Option("b", BUCKET_OPTION, true, "Bucket to group metrics by when writing" +
                " to cassandra");

        options.addOption(sourceTopic);
        options.addOption(windowDurationSeconds);
        options.addOption(clientId);
        options.addOption(bucket);

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

    public static void runCassandraSink(long windowDurationSeconds, ChronoUnit bucket) {
        try (CqlSession session = CqlSession.builder().build()) {
            String tableName = CassandraSink.getTargetTableName(windowDurationSeconds);
            CassandraDao dao = new CassandraDao(session, tableName);
            CassandraSink sink = new CassandraSink(dao, bucket, windowDurationSeconds);
            sink.consume();
        }
    }
}
