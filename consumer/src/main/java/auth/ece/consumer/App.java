package auth.ece.consumer;

import lombok.extern.log4j.Log4j2;

public class App {

    public static void main(String[] args) {
        ConsoleConsumer consumer = new ConsoleConsumer();
        executeConsumer(consumer);
    }

    public static void executeConsumer(ConsoleConsumer consumer) {
        consumer.consume("metrics");
    }
}
