package com.aabir.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class ProducerInterceptorConfig implements ProducerInterceptor<String, String> {

    private ObservationRegistry observationRegistry;

    private Observation observation;

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        // This code will be called before the message gets sent. We create
        // a context and pass it to an Observation. Upon start, the handler will be called
        // and the ProducerRecord will be mutated
        KafkaSenderContext context = new KafkaSenderContext(record);
        this.observation = Observation.start("kafka.send", () -> context, observationRegistry);
        // We return the mutated carrier
        return context.getCarrier();
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // Once the message got sent (with or without an exception) we attach an exception
        // and stop the observation
        this.observation.error(exception);
        this.observation.stop();
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {
        // We retrieve the ObservationRegistry from the configuration
        this.observationRegistry = (ObservationRegistry) configs.get(ObservationRegistry.class.getName());
    }

}
