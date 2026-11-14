package com.example.mentalhealth;

public record StudentRecord(int age, String gender, String country, String academicLevel,
        String mostUsedPlatform, String purposeOfUse, double avgDailyUsageHours,
        int dailyUnlocks, double studyHours, double physicalActivityHours,
        double sleepHoursPerNight, String stressLevel) { }
