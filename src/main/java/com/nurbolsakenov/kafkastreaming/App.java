package com.nurbolsakenov.kafkastreaming;

import java.util.Properties;
import java.util.Arrays;
import java.util.Locale;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.log4j.PropertyConfigurator;


public class App 
{
    public static void main( String[] args )
    {
        String log4jConfPath = "src/main/java/com/nurbolsakenov/resources/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafkastreaming");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName()); 
        
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("word-count-input");

        final KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
                .groupBy((key, value) -> value)
                .count();
        
        textLines.foreach( 
            (key, value) -> {
                System.out.println("(DSL) Hello, " + value);
            });

                
        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // add shutdown hook to stop the application gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}
