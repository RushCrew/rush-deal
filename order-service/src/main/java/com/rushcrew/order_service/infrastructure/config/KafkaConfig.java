package com.rushcrew.order_service.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 replica에 전송 확인
		configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
		configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 멱등성 보장
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	/**
	 * Consumer 설정
	 * - ErrorHandlingDeserializer로 래핑하여 역직렬화 오류 방지
	 * - 실제 역직렬화는 StringDeserializer 사용
	 */
	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		// ErrorHandlingDeserializer를 메인 Deserializer로 설정
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		// 실제 역직렬화에 사용할 Deserializer 지정
		configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
		configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);	// 수동 커밋 --> 메시지 처리 성공 시에만 커밋되므로 데이터 손실을 방지할 수 있음
		// 세션 타임아웃 설정
		configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
		configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
		return new DefaultKafkaConsumerFactory<>(configProps);
	}

	/**
	 * Kafka Listener Container 설정
	 * - RECORD 단위 ACK (메시지 단위 커밋)
	 * - 수동 커밋 모드 사용
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		// RECORD 단위 ACK: 메시지 처리 성공 시 즉시 커밋
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		return factory;
	}
}

