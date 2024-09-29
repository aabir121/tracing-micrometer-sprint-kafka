package com.aabir.tracing;

import io.micrometer.observation.transport.SenderContext;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

public class KafkaSenderContext extends SenderContext<ProducerRecord<String, String>> {

    public KafkaSenderContext(ProducerRecord<String, String> producerRecord) {
        // We describe how the carrier will be mutated (we mutate headers)
        super((carrier, key, value) -> carrier.headers().add(key, value.getBytes(StandardCharsets.UTF_8)));
        setCarrier(producerRecord);
    }

}
