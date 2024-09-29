package com.aabir.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.observation.KafkaRecordSenderContext;
import reactor.kafka.sender.observation.KafkaSenderObservation;

@Service
@Slf4j
public class ReactiveKafkaMessageProducerService {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate;
    private final String topic;
    private final ObservationRegistry observationRegistry;

    private final SenderOptions<String, Object> senderOptions;

    public ReactiveKafkaMessageProducerService(ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate,
                                               @Value("${spring.kafka.topic.default-topic}") String topic,
                                               ObservationRegistry observationRegistry,
                                               SenderOptions<String, Object> senderOptions) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.topic = topic;
        this.observationRegistry = observationRegistry;
        this.senderOptions = senderOptions;
    }

    public Mono<Void> sendMessage(String key, String message) {
        log.info("Sending message {} with key {}", message, key);
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, message);

        return kafkaProducerTemplate.send(record)
                .doOnSuccess(result -> log.info("Sent message: " + message + " to topic: " + topic))
                .doOnError(ex -> log.error("Error sending message: {}", ex.getMessage()))
                .then();
    }
}