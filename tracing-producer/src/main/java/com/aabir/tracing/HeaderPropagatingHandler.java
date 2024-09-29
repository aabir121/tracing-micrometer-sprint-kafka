package com.aabir.tracing;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.MDC;

public class HeaderPropagatingHandler implements ObservationHandler<KafkaSenderContext> {

    @Override
    public void onStart(KafkaSenderContext context) {
        context.getSetter().set(context.getCarrier(), "foo", "bar");
        context.getSetter().set(context.getCarrier(), "traceId", MDC.getCopyOfContextMap().get("traceId"));
        context.getSetter().set(context.getCarrier(), "spanId", MDC.getCopyOfContextMap().get("spanId"));
        context.addLowCardinalityKeyValue(KeyValue.of("sent", "true"));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof KafkaSenderContext;
    }

}
