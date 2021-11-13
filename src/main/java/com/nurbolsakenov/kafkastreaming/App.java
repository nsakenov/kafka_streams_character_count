package com.nurbolsakenov.kafkastreaming;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.log4j.PropertyConfigurator;

public final class App {
    public static final String INPUT_TOPIC = "word-count-input";
    public static final String OUTPUT_TOPIC = "word-count-output";

    static Properties getStreamsConfig(final String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafkastreaming");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        config.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 0);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return config;
    }

    static void createWordCountStream(final StreamsBuilder builder) {
        final KStream<String, String> source = builder.stream(INPUT_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String()));

        final KTable<Windowed<String>, Long> test = source
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
                .groupBy((key, word) -> word, Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(1)))
                .count(Materialized.as("test"));

        test.toStream().map((key, value) -> KeyValue.pair(key.key(), key.key() + ": " + value.toString()))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        source.foreach((key, value) -> {
            System.out.println("(DSL): " + value);
        });
    }

    public static void main(final String[] args) {
        String log4jConfPath = "src/main/java/com/nurbolsakenov/resources/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        final Properties props = getStreamsConfig(args);

        final StreamsBuilder builder = new StreamsBuilder();
        createWordCountStream(builder);

        Topology topology = builder.build();

        System.out.println(topology.describe());
        final KafkaStreams streams = new KafkaStreams(topology, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();

            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}