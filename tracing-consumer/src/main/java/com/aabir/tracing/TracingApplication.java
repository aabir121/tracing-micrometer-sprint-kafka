package com.aabir.tracing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@RequiredArgsConstructor
public class TracingApplication {

	public static void main(String[] args) {
//		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(TracingApplication.class, args);
	}

}
