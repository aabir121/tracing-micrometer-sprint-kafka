package com.aabir.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class ConsumerInterceptorConfig implements ConsumerInterceptor<String, String> {
    private ObservationRegistry observationRegistry;
    @Override
    public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {
        // We're creating the receiver context
        KafkaReceiverContext context = new KafkaReceiverContext(records);
        // Then, we're just starting and stopping the observation on the consumer side
        Observation.start("kafka.receive", () -> context, observationRegistry);
        // We could put the Observation in scope so that the users can propagate it
        // further on
        log.info("on consumer interceptor");
        return context.getCarrier();
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {

    }

    @Override
    public void close() {
        Objects.requireNonNull(this.observationRegistry.getCurrentObservation()).stop();
    }

    @Override
    public void configure(Map<String, ?> map) {
        this.observationRegistry = (ObservationRegistry) map.get(ObservationRegistry.class.getName());
    }
}
