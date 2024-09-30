package com.aabir.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
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
                .flatMap(record -> {
                    log.info("Initial one and will not have correlation {}", record);
                    return Flux.just(record);
                })
                .flatMap(this::startObservation) // Abstracted observation starting
                .flatMap(record -> {
                    log.info("This log should still have the correlation {}", record);
                    return Flux.just(record);
                })
                .flatMap(record -> Mono.deferContextual(contextView -> {
                    Observation observation = contextView.get(ObservationThreadLocalAccessor.KEY);

                    log.info("We are now using the same observation {}", observation);
                    return Mono.just(record)
                            .flatMap(this::handleRecord);  // Replace with actual record handler logic
                }))
                .contextCapture();  // Automatically capture and propagate context downstream
    }

    // A method to start observation once and propagate it downstream
    private Mono<ConsumerRecord<String, String>> startObservation(ConsumerRecord<String, String> record) {
        Observation receiverObservation = KafkaReceiverObservation.RECEIVER_OBSERVATION.start(
                null,
                KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                () -> new KafkaRecordReceiverContext(record, "user.receiver", this.receiverOptions.bootstrapServers()),
                this.observationRegistry);

        return Mono.just(record)
                .doOnNext(rec -> log.info("First chain with correlation {}", rec))
                .doOnNext(rec -> log.info("Second chain with correlation {}", rec))
                .doOnNext(rec -> log.info("Third chain with correlation {}", rec))
                .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation))
                .doOnError(receiverObservation::error)
                .doOnTerminate(receiverObservation::stop);
    }

    // A placeholder method to handle your records
    private Mono<String> handleRecord(ConsumerRecord<String, String> record) {
        log.info("Handling record: {}", record.value());
        return Mono.just(record.value());
    }
}
