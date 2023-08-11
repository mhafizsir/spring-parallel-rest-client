package com.example.java8spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootApplication
public class SpringParallelRestClient {

    public static void main(String[] args) {
        SpringApplication.run(SpringParallelRestClient.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {

        AtomicReference<Integer> failCount = new AtomicReference<>(0);
        AtomicReference<Integer> successCount = new AtomicReference<>(0);
        AtomicReference<Integer> requestCount = new AtomicReference<>(0);
        return args -> {
            ExecutorService executor = Executors.newFixedThreadPool(10); // adjust the thread pool size as needed

            for (int i = 0; i < 1000; i++) { // adjust the number of requests as needed
                int finalI = i;
                executor.submit(() -> {

                    String url = "http://localhost:9090" + finalI; // replace with actual URL
                    log.info("Sending request to: " + url);
                    log.info("Thread being used for this request: " + Thread.currentThread().getName());
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders()), String.class);
                    log.info("Received response with status code: " + response.getStatusCode());
                    log.info("Response body: " + response.getBody());

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.getAndSet(successCount.get() + 1);
                    } else {
                        failCount.getAndSet(failCount.get() + 1);
                    }
                    requestCount.getAndSet(requestCount.get() + 1);
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            log.info("Fail count: " + failCount.get());
            log.info("Success count: " + successCount.get());
            log.info("Request count: " + requestCount.get());
        };
    }
}
