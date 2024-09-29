package com.aabir.tracing;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.ArrayList;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    SenderOptions<String, Object> senderOptions(KafkaProperties kafkaProperties, ObservationRegistry registry) {
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties(null);
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", (ArrayList<String>) configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)));
        configProps.put(ObservationRegistry.class.getName(), registry);
        configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                "com.aabir.tracing.ProducerInterceptorConfig");
        return SenderOptions.create(configProps);  // Enabling Observation
    }


    @Bean
    public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(SenderOptions<String, Object> senderOptions,
                                                                                       ObservationRegistry registry) {

        registry.observationConfig().observationHandler(new HeaderPropagatingHandler());
        log.info("Producer template setup done");
        return new ReactiveKafkaProducerTemplate<>(KafkaSender.create(senderOptions.withObservation(registry)));
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

}