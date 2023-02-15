package auth.ece.consumer;

import org.apache.commons.cli.*;
import org.apache.kafka.common.quota.ClientQuotaAlteration;

public class App {

    private static final String SOURCE_TOPIC_OPTION = "sourceTopic";
    private static final String WINDOW_DURATION_SECONDS_OPTION = "windowDurationSeconds";

    private static final String CLIENT_ID_OPTION = "clientId";

    public static void main(String[] args) {
        CommandLine cli = parseArgs(args);
        String sourceTopic = cli.getOptionValue(SOURCE_TOPIC_OPTION);
        long windowDurationSeconds = Long.parseLong(cli.getOptionValue(WINDOW_DURATION_SECONDS_OPTION));
        int clientId = Integer.parseInt(cli.getOptionValue(CLIENT_ID_OPTION));
//        ConsoleConsumer consumer = new ConsoleConsumer();
//        executeConsumer(consumer);
        MetricsAggregator aggregator = new MetricsAggregator(windowDurationSeconds, sourceTopic, clientId);
        aggregator.start();
    }
    public static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        Option sourceTopic = new Option("s", SOURCE_TOPIC_OPTION, true, "Source topic");

        Option windowDurationSeconds = new Option("w", WINDOW_DURATION_SECONDS_OPTION, true,
                "Aggregation window duration in seconds");

        Option clientId = new Option("c", CLIENT_ID_OPTION, true, "Client id to differentiate" +
                " Kafka Streams apps. Make sure that this is unique per process for processes running in the same host");

        options.addOption(sourceTopic);
        options.addOption(windowDurationSeconds);
        options.addOption(clientId);

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
}
