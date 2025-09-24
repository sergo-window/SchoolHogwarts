package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Stream;

@RestController
public class InfoController {

    @Value("${server.port:0}")
    private int port;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/port")
    public int getPort() {
        return port;
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return "Active profile: " + activeProfile;
    }

    @GetMapping("/sum/original")
    public Map<String, Object> getSumOriginal() {
        long startTime = System.currentTimeMillis();

        int sum = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, Integer::sum);

        long executionTime = System.currentTimeMillis() - startTime;

        return Map.of(
                "sum", sum,
                "method", "original_stream",
                "execution_time_ms", executionTime
        );
    }

    @GetMapping("/sum/optimized")
    public Map<String, Object> getSumOptimized() {
        long startTime = System.nanoTime();
        long n = 1_000_000;
        long sum = n * (n + 1) / 2;

        long executionTimeNanos = System.nanoTime() - startTime;

        return Map.of(
                "result", sum,
                "method", "arithmetic_progression",
                "execution_time_nanoseconds", executionTimeNanos,
                "execution_time_milliseconds", executionTimeNanos / 1_000_000.0,
                "formula", "n × (n + 1) ÷ 2 where n = 1,000,000"
        );
    }
}
