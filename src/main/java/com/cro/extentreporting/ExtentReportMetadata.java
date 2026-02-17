package com.cro.extentreporting;

import java.util.LinkedHashMap;
import java.util.Map;
import com.aventstack.extentreports.service.ExtentService;

/**
 * Simple metadata collector for Extent Report.
 * Sequential execution - no thread safety needed.
 */
public class ExtentReportMetadata {

    private static final Map<String, String> SYSTEM_INFO = new LinkedHashMap<>();
    private static boolean published = false;

    private ExtentReportMetadata() {}

    public static void put(String key, String value) {
        if (value == null || value.isBlank()) return;
        SYSTEM_INFO.put(key, value);
    }

    public static void publishOnce() {
        if (published) return;
        
        SYSTEM_INFO.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .forEach(e ->
                ExtentService.getInstance()
                    .setSystemInfo(e.getKey(), e.getValue())
            );
        
        published = true;
    }
}