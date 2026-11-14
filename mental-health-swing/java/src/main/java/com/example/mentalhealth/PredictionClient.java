package com.example.mentalhealth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class PredictionClient {
    private static final String API_URL = System.getenv().getOrDefault("ML_API_URL", "http://localhost:8000/predict");
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private PredictionClient() { }

    public static double predict(StudentRecord s) throws Exception {
        String json = String.format(java.util.Locale.US,
                "{\"age\":%d,\"gender\":\"%s\",\"country\":\"%s\",\"academic_level\":\"%s\",\"most_used_platform\":\"%s\",\"purpose_of_use\":\"%s\",\"avg_daily_usage_hours\":%.3f,\"daily_unlocks\":%d,\"study_hours\":%.3f,\"physical_activity_hours\":%.3f,\"sleep_hours_per_night\":%.3f,\"stress_level\":\"%s\"}",
                s.age(), escape(s.gender()), escape(s.country()), escape(s.academicLevel()), escape(s.mostUsedPlatform()), escape(s.purposeOfUse()), s.avgDailyUsageHours(), s.dailyUnlocks(), s.studyHours(), s.physicalActivityHours(), s.sleepHoursPerNight(), escape(s.stressLevel()));
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new IllegalStateException("ML API returned HTTP " + response.statusCode() + ": " + response.body());
        var matcher = java.util.regex.Pattern.compile("\\\"predicted_mental_health_score\\\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)").matcher(response.body());
        if (!matcher.find()) throw new IllegalStateException("Unexpected ML API response: " + response.body());
        return Double.parseDouble(matcher.group(1));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
