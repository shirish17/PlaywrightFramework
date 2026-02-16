package com.cro.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class PropertiesLoader {
    private static final Properties PROPERTIES = new Properties();
    private static boolean loaded = false;

    private PropertiesLoader() {}

    public static synchronized void load() {
        if (loaded) return;

        // Load mandatory defaults from config folder
        loadFile("config/app-default.properties", true);
        loadFile("config/test-default.properties", true);

        // Load environment-specific (optional)
        String env = System.getProperty("env");
        if (env != null && !env.isBlank()) {
            loadFile("config/" + env + ".properties", false);
        }

     // CLI overrides EVERYTHING (highest priority)
        System.getProperties().forEach((key, value) -> PROPERTIES.put(key, value));
        
        // DEBUG: Print what's actually loaded
        System.out.println("\n========== DEBUG: ALL USER/PASS PROPERTIES ==========");
        PROPERTIES.stringPropertyNames().stream()
            .filter(key -> key.startsWith("user.") || key.startsWith("pass.") || key.startsWith("tenant."))
            .sorted()
            .forEach(key -> System.out.println("  " + key + " = " + PROPERTIES.getProperty(key)));
        System.out.println("====================================================\n");
        
        loaded = true;
        System.out.println("Properties loaded successfully");
    }

    private static void loadFile(String fileName, boolean mandatory) {
        try (InputStream is = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (is != null) {
                PROPERTIES.load(is);
                System.out.println("Loaded: " + fileName);
            } else if (mandatory) {
                throw new RuntimeException("Mandatory property file not found: " + fileName);
            } else {
                System.out.println("Optional file not found (skipped): " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed loading property file: " + fileName, e);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null)
            throw new RuntimeException("Missing property: " + key);
        return value;
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String getBrowser() {
        String browser = get("browser");
        return browser.replace("__", "").toLowerCase().trim();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "true"));
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
    
    /**
     * Get build ID for session isolation.
     * Priority:
     * 1. CLI override: -Dbuild.id=BUILD_12345 (for CI/CD)
     * 2. System property from Maven: build.id (timestamp from pom.xml)
     * 3. Fallback: Generate timestamp in format d-MMM-yy_HH-mm-ss
     */
    public static String getBuildId() {
        String buildId = System.getProperty("build.id");
        
        if (buildId != null && !buildId.isBlank() && !"${maven.build.timestamp}".equals(buildId)) {
            return buildId;
        }
        
        // Generate timestamp: 13-Feb-26_17-45-30
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yy_HH-mm-ss");
        return LocalDateTime.now().format(formatter);
    }
 // ========== NEW: TIMEOUT METHODS ==========

    /**
     * Get element timeout in milliseconds (default: 10000ms = 10s)
     */
    public static int getElementTimeout() {
        return Integer.parseInt(get("timeout.element", "10000"));
    }

    /**
     * Get page timeout in milliseconds (default: 30000ms = 30s)
     */
    public static int getPageTimeout() {
        return Integer.parseInt(get("timeout.page", "30000"));
    }
    /**
     * Get tenant name for a specific role.
     * Returns null if user is single-tenant (no tenant.{role} property).
     * 
     * @param role User role (e.g., "creator", "editor")
     * @return Tenant name or null if single-tenant
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
     * Check if a role is multi-tenant.
     * 
     * @param role User role
     * @return true if tenant.{role} property exists
     */
    public static boolean isMultiTenant(String role) {
        return getTenantForRole(role) != null;
    }
}