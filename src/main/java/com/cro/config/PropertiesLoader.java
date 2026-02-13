package com.cro.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesLoader {
	private static final Properties PROPERTIES = new Properties();
    private static boolean loaded = false;

    private PropertiesLoader() {}

    public static synchronized void load() {
        if (loaded) return;

        loadFile("app-default.properties");
        loadFile("test-default.properties");

        String env = System.getProperty("env");
        if (env != null && !env.isBlank()) {
            loadFile(env + ".properties");
        }

        // CLI overrides
        System.getProperties().forEach((k, v) ->
                PROPERTIES.put(k, v));

        loaded = true;
    }

    private static void loadFile(String fileName) {
        try (InputStream is = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (is != null) {
                PROPERTIES.load(is);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed loading property file: " + fileName, e);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null)
            throw new RuntimeException("Missing property: " + key);
        return value;
    }

    public static String getBrowser() {
        return get("browser");
    }

    public static String getBaseUrl() {
        return get("base.url");
    }

    public static String getUsername(String role) {
        return get("user." + role);
    }

    public static String getPassword(String role) {
        return get("pass." + role);
    }
}
