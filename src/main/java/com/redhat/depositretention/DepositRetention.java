package com.redhat.depositretention;

import com.redhat.depositretention.drools.DroolsRulesApplier;
import org.apache.camel.builder.RouteBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
public class DepositRetention {

	public static final String BROKER_URL = "localhost:9092";
	public static final String INPUT_TOPIC = "event-input-stream";
	public static final String OUTPUT_TOPIC = "offer-output-stream";



	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public StreamsConfig kStreamsConfigs(KafkaProperties kafkaProperties) {
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testStreams");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		return new StreamsConfig(props);
	}

	@Bean
	public KStream<String, String> kStream(StreamsBuilder builder) {

		DroolsRulesApplier rulesApplier = new DroolsRulesApplier();
		final KStream<String, String> inputTopic = builder.stream(INPUT_TOPIC);

		KStream<String, String> outputData = inputTopic.map((x,y) -> new KeyValue<>(x,rulesApplier.processTransaction(x,y)));

		outputData.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

		return outputData;
	}


	public static void main(String[] args) {
		SpringApplication.run(DepositRetention.class, args);
	}
}
