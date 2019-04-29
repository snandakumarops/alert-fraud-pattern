package com.redhat.depositretention;

import com.google.gson.Gson;
import com.redhat.depositretention.drools.DroolsRulesApplier;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;


import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
public class EventAnalysis {


	@Value("${broker.url}")
	private String brokerUrl;

	@Value("${broker.input.topic}")
	private String inpTopic;

	@Value("${broker.output.topic}")
	private String outTopic;

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public StreamsConfig kStreamsConfigs(KafkaProperties kafkaProperties) {
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testStreams");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		return new StreamsConfig(props);
	}

	@Bean
	public KStream<String, String> kStream(StreamsBuilder builder) {

		DroolsRulesApplier rulesApplier = new DroolsRulesApplier();
		final KStream<String, String> inputTopic = builder.stream(inpTopic);


		KStream<String, String> outputData = inputTopic.map((x,y) -> new KeyValue<>(x,rulesApplier.processEvent(x,y)));
		//Branch all not null events
		KStream<String, String>[] analyzedEvents = outputData.branch((x, y) -> null != y);
		analyzedEvents[0].to(outTopic, Produced.with(Serdes.String(), Serdes.String()));

		return outputData;
	}


	public static void main(String[] args) {
		SpringApplication.run(EventAnalysis.class, args);
	}
}
