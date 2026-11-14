package com.example.mentalhealth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnalyticsClient {
    private static final String API_URL = System.getenv().getOrDefault("ML_API_URL", "http://localhost:8000/predict");
    private static final String API_BASE = API_URL.replaceFirst("/predict/?$", "");
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Pattern NUMBER = Pattern.compile("\"([a-z0-9_]+)\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?(?:[Ee][+-]?[0-9]+)?)");

    private AnalyticsClient() { }

    public static Map<String, Double> fetch() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE + "/analytics"))
                .timeout(Duration.ofSeconds(20)).GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new IllegalStateException("Analytics API returned HTTP " + response.statusCode());
        Map<String, Double> values = new LinkedHashMap<>();
        Matcher matcher = NUMBER.matcher(response.body());
        while (matcher.find()) values.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
        if (!values.containsKey("sample_size")) throw new IllegalStateException("The analytics response was incomplete.");
        return values;
    }
}
