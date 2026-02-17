package com.cro.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Properties loader with fallback chain:
 * CLI → env-specific → test-default → app-default
 */
public class PropertiesLoader {
    
    private static final Properties PROPERTIES = new Properties();
    private static boolean loaded = false;

    private PropertiesLoader() {}

    /**
     * Load properties with fallback chain.
     * Called once at @BeforeAll.
     */
    public static synchronized void load() {
        if (loaded) return;

        // Load in reverse priority order (lowest to highest)
        loadFile("config/app-default.properties", false);
        loadFile("config/test-default.properties", false);

        // Load env-specific file if -Denv provided
        String env = System.getProperty("env");
        if (env != null && !env.isBlank()) {
            loadFile("config/" + env + ".properties", false);
        }

        // CLI overrides everything
        System.getProperties().forEach((key, value) -> PROPERTIES.put(key, value));

        loaded = true;
        
        // Validate mandatory properties
        validateMandatoryProperties();
        
        System.out.println("Properties loaded successfully");
    }

    /**
     * Validate mandatory properties at startup.
     * Fail fast if missing.
     */
    private static void validateMandatoryProperties() {
        String baseUrl = PROPERTIES.getProperty("base.url");
        
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException(
                "\n╔════════════════════════════════════════════════════════╗\n" +
                "║  CONFIGURATION ERROR: Missing base.url                 ║\n" +
                "║                                                        ║\n" +
                "║  Please provide base.url via:                         ║\n" +
                "║  1. CLI: -Dbase.url=https://...                       ║\n" +
                "║  2. env.properties file: base.url=https://...         ║\n" +
                "║  3. test-default.properties: base.url=https://...     ║\n" +
                "╚════════════════════════════════════════════════════════╝"
            );
        }
        
        System.out.println("✓ Mandatory properties validated");
    }

    /**
     * Load a properties file.
     */
    private static void loadFile(String fileName, boolean mandatory) {
        try (InputStream is = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (is != null) {
                PROPERTIES.load(is);
                System.out.println("Loaded: " + fileName);
            } else if (mandatory) {
                throw new RuntimeException("Mandatory property file not found: " + fileName);
            }
        } catch (IOException e) {
            if (mandatory) {
                throw new RuntimeException("Failed loading property file: " + fileName, e);
            }
        }
    }

    /**
     * Get property value.
     */
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing property: " + key);
        }
        return value;
    }

    /**
     * Get property with default.
     */
    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    /**
     * Get browser (with fallback to chrome).
     * Supports: chrome, chromium, firefox, edge, msedge, webkit
     * Case insensitive.
     */
    public static String getBrowser() {
        String browser = get("browser", "chrome").toLowerCase().trim();
        
        // Normalize browser names
        if (browser.equals("edge")) {
            browser = "msedge";
        }
        
        return browser;
    }

    /**
     * Get headless flag (default: false = headed).
     */
    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    /**
     * Get base URL (mandatory, validated at startup).
     */
    public static String getBaseUrl() {
        return get("base.url");
    }

    /**
     * Get username for role.
     * Validates that user.{role} exists.
     */
    public static String getUsername(String role) {
        String key = "user." + role;
        String username = PROPERTIES.getProperty(key);
        
        if (username == null || username.isBlank()) {
            throw new RuntimeException(
                "Missing property: " + key + "\n" +
                "Please add: user." + role + "=<email> to your properties file"
            );
        }
        
        return username;
    }

    /**
     * Get password for role.
     * Validates that pass.{role} exists.
     */
    public static String getPassword(String role) {
        String key = "pass." + role;
        String password = PROPERTIES.getProperty(key);
        
        if (password == null || password.isBlank()) {
            throw new RuntimeException(
                "Missing property: " + key + "\n" +
                "Please add: pass." + role + "=<password> to your properties file"
            );
        }
        
        return password;
    }

    /**
     * Get build ID for session isolation.
     * Uses -Dbuild.id if provided, otherwise timestamp.
     */
    public static String getBuildId() {
        String buildId = System.getProperty("build.id");
        
        if (buildId != null && !buildId.isBlank() && !"${maven.build.timestamp}".equals(buildId)) {
            return buildId;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yy_HH-mm-ss");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Get element timeout (default: 10000ms).
     */
    public static int getElementTimeout() {
        return Integer.parseInt(get("timeout.element", "10000"));
    }

    /**
     * Get page timeout (default: 30000ms).
     */
    public static int getPageTimeout() {
        return Integer.parseInt(get("timeout.page", "30000"));
    }

    /**
     * Get tenant for role (optional).
     * Returns null if not multi-tenant.
     */
    public static String getTenantForRole(String role) {
        String tenantKey = "tenant." + role;
        String tenantName = PROPERTIES.getProperty(tenantKey);
        
        if (tenantName == null || tenantName.isBlank()) {
            return null;
        }
        
        return tenantName.trim();
    }

    /**
     * Check if role is multi-tenant.
     */
    public static boolean isMultiTenant(String role) {
        return getTenantForRole(role) != null;
    }
}