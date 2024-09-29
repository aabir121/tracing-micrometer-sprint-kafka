package com.aabir.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;

@Service
@Slf4j
public class ReactiveKafkaMessageConsumerService {

    private final ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
    private final ObservationRegistry observationRegistry;
    private final ReceiverOptions<String, String> receiverOptions;

    public ReactiveKafkaMessageConsumerService(ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate,
                                               ObservationRegistry observationRegistry,
                                               ReceiverOptions<String, String> receiverOptions) {
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
        this.observationRegistry = observationRegistry;
        this.receiverOptions = receiverOptions;
    }

    public Flux<String> consumeMessages() {
        log.info("Inside consume messages");

        return kafkaConsumerTemplate.receiveAutoAck()
                .flatMap(record -> Mono.deferContextual(contextView -> {
                    return Mono.just(record)
                            .flatMap(this::handleRecord);  // Replace with actual record handler logic
                }));
    }

    // A placeholder method to handle your records
    private Mono<String> handleRecord(ConsumerRecord<String, String> record) {
        log.info("Handling record: {}", record.value());
        return Mono.just(record.value());
    }
}
