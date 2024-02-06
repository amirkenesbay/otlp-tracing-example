package com.amirkenesbay.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {
    private final RestTemplate restTemplate;
    private final Tracer tracer;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        Span span = Span.current();
        ResponseEntity<String> response = restTemplate.postForEntity("https://httpbin.org/post", "Hello, Cloud!", String.class);
        log.info("Response: " + response);
        span.setAttribute("Example", "test");
        log.info("Span context: ", span.getSpanContext());
        return response;
    }


    @GetMapping("/v2/hello")
    public ResponseEntity<String> helloV2() {
        Span parentSpan = tracer.spanBuilder("parent").startSpan();
        parentSpan.setAttribute("Example", "parentSpanTest");
        parentSpan.end();

        Span childSpan = tracer.spanBuilder("child").setParent(Context.current().with(parentSpan)).startSpan();
        ResponseEntity<String> response = restTemplate.postForEntity("https://httpbin.org/post", "Hello, Cloud!", String.class);
        childSpan.setAttribute("Example", "childSpanTest");

        childSpan.end();
        log.info("Response: " + response);
        return response;
    }
}