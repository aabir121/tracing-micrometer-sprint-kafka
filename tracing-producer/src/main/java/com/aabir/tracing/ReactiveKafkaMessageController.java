package com.aabir.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ReactiveKafkaMessageController {

    private final ReactiveKafkaMessageProducerService kafkaMessageProducerService;
    private final WebClient webClient;

    public ReactiveKafkaMessageController(ReactiveKafkaMessageProducerService kafkaMessageProducerService, WebClient webClient) {
        this.kafkaMessageProducerService = kafkaMessageProducerService;
        this.webClient = webClient;
    }

    @GetMapping("/send")
    public Mono<Void> sendMessage(@RequestParam String key, @RequestParam String message) {
        log.info("Sending message from controller {} and {}", key, message);

        return kafkaMessageProducerService.sendMessage(key, message);
    }

    @GetMapping("/send-consumer")
    public Mono<String> sendToApi() {
        return webClient.get()
                .uri("http://localhost:8081/hello")
                .retrieve()
                .bodyToMono(String.class);
    }
}