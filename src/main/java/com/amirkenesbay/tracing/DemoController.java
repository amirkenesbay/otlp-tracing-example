package com.amirkenesbay.tracing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DemoController {
    private final RestTemplate restTemplate;

    @GetMapping("/user-trigger")
    public String start() {
        String url = "http://localhost:8082/downstream";

        log.info("Sending request to {}", url);

        String response = restTemplate.getForObject(url, String.class);

        log.info("Received response: {}", response);

        return "Response from A: " + response;
    }

    @GetMapping("/downstream")
    public String callReversedServices(HttpServletRequest request) {
        int serverPort = request.getServerPort();
        log.info("Received call on port {}", serverPort);

        if (serverPort < 8084) {
            String nextUrl = "http://localhost:" + (serverPort + 1) + "/downstream";

            log.info("Forwarding call to {}", nextUrl);

            String response = restTemplate.getForObject(nextUrl, String.class);

            log.info("Received response: {}", response);

            return "Response from " + serverPort + ": " + response;
        } else {
            log.info("Chain of calls ends at port {}", serverPort);

            return "Chain of calls ends at " + serverPort;
        }
    }
}
