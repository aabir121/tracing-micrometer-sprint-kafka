package com.aabir.tracing;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.MDC;

public class HeaderReadingHandler implements ObservationHandler<KafkaReceiverContext> {
    @Override
    public void onStart(KafkaReceiverContext context) {
//        String traceId = context.getGetter().get(context.getCarrier(), "traceId");
//        MDC.put("traceId", traceId);
//        String spanId = context.getGetter().get(context.getCarrier(), "spanId");
//        MDC.put("spanId", spanId);
//        // We're setting the value of the <foo> header as a low cardinality key value
//        context.addLowCardinalityKeyValue(KeyValue.of("received traceid header", traceId));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof KafkaReceiverContext;
    }
}
