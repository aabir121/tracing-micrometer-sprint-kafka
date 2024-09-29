package com.aabir.tracing;

import io.micrometer.observation.transport.ReceiverContext;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;

public class KafkaReceiverContext extends ReceiverContext<ConsumerRecords<String, String>> {

    public KafkaReceiverContext(ConsumerRecords<String, String> consumerRecord) {
        // We describe how to read entries from the carrier (we read headers)
        super((carrier, key) -> {
            // This is a very naive approach that takes the first ConsumerRecord
            Header header = carrier.iterator().next().headers().lastHeader(key);
            if (header != null) {
                return new String(header.value());
            }
            return null;
        });
        setCarrier(consumerRecord);
    }

}
