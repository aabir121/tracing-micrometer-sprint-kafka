package com.aabir.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ReactiveKafkaMessageConsumerController {

    private final ReactiveKafkaMessageConsumerService kafkaMessageConsumerService;

    public ReactiveKafkaMessageConsumerController(ReactiveKafkaMessageConsumerService kafkaMessageConsumerService) {
        this.kafkaMessageConsumerService = kafkaMessageConsumerService;
        kafkaMessageConsumerService.consumeMessages()
                .doOnNext(record -> {
                    log.info("This is consumer record {}", record);
                }).doOnNext(record -> {
                    log.info("Second time inside operator with {}", record);
                }).flatMap(record -> {
                    log.info("Inside a flat map now with {}", record);
                    return Flux.just(record);
                }).then();
    }

    @GetMapping("/consume")
    public Flux<String> consumeMessages() {
        log.info("Consume message controller");
        return kafkaMessageConsumerService.consumeMessages()
                .doOnNext(record -> {
                    log.info("This is consumer record {}", record);
                });
    }

    @GetMapping("/hello")
    public Mono<String> hello() {
        log.info("Consume message hello");
        return Mono.just("hello from consumer");
    }
}
