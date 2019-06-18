package com.redhat.eventAnalysis;

import com.redhat.eventAnalysis.processor.EventProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
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
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		return new StreamsConfig(props);
	}

	@Bean
	public KStream<String, String> kStream(StreamsBuilder builder) {

		EventProcessor rulesApplier = new EventProcessor();
		final KStream<String, String> inputTopic = builder.stream(inpTopic);

		//Watch for ATM Withdrawal events within a time window
		KStream<Windowed<String>, Long>[] sream = inputTopic.map((x, y) -> new KeyValue<>(x,rulesApplier.processEvent(x,y)))
				.filter((k,v) -> null != v)
				.groupByKey()
				.windowedBy(TimeWindows.of(60000L))
				.count()
				.filter((k,v) -> v > 2)
				.toStream()
				.branch((k,v) -> null != v);
		sream[0].foreach((k,v) -> System.out.println(k.key()));
		KStream<String, String> stream = sream[0]
				.map((k,v)-> new KeyValue<>(k.key(),String.valueOf(v)));

		stream.to(outTopic);

		return stream;
	}


	public static void main(String[] args) {
		SpringApplication.run(EventAnalysis.class, args);
	}
}
