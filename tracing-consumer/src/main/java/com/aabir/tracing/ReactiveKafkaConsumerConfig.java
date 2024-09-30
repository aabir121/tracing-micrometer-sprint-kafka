package com.aabir.tracing;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ReactiveKafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.topic.default-topic}")
    private String topic;

    @Bean
    public ReceiverOptions<String, String> receiverOptions(KafkaProperties kafkaProperties,
                                                           ObservationRegistry registry) {
        Map<String, Object> configProps = kafkaProperties.buildConsumerProperties(null);
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", (ArrayList<String>) configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)));
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ObservationRegistry.class.getName(), registry);
//        configProps.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
//                "com.aabir.tracing.ConsumerInterceptorConfig");

        return ReceiverOptions.<String, String>create(configProps)
                .subscription(Collections.singleton(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate(ReceiverOptions<String, String> receiverOptions,
                                                                                       ObservationRegistry observationRegistry,
                                                                                       PropagatingReceiverTracingObservationHandler<?> handler) {
//        observationRegistry.observationConfig().observationHandler(new PropagatingReceiverTracingObservationHandler<>(tracer, propagator));
        observationRegistry.observationConfig().observationHandler(handler);
        ObservationThreadLocalAccessor.getInstance().setObservationRegistry(observationRegistry);
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions.withObservation(observationRegistry));
    }
}