package com.cro.hooks;

import com.cro.config.PropertiesLoader;
import com.cro.extentreporting.ExtentReportMetadata;
import com.cro.playwright.BrowserManager;
import com.cro.playwright.SessionManager;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

/**
 * Global hooks for framework initialization and cleanup.
 */
public class GlobalHooks {

	 @BeforeAll
	    public static void beforeAll() {
	        System.out.println("========================================");
	        System.out.println("🚀 FRAMEWORK INITIALIZATION");
	        System.out.println("========================================");

	        // Load and validate properties
	        PropertiesLoader.load();

	        String env       = System.getProperty("env", "test-default");
	        String buildId   = PropertiesLoader.getBuildId();
	        String browser   = PropertiesLoader.getBrowser();
	        boolean headless = PropertiesLoader.isHeadless();
	        String baseUrl   = PropertiesLoader.getBaseUrl();
	        String projectDir = System.getProperty("user.dir");

	        System.out.println("Build ID : " + buildId);
	        System.out.println("Browser  : " + browser);
	        System.out.println("Headless : " + headless);
	        System.out.println("Base URL : " + baseUrl);
	        System.out.println("Env      : " + env);

	        // Initialize browser
	        BrowserManager.initBrowser(browser);

	        // Get browser version
	        String browserVersion = getBrowserVersion(browser);

	        // Publish metadata to Extent Report
	        ExtentReportMetadata.put("app",                   "CTMS Application");
	        ExtentReportMetadata.put("Base Directory",        projectDir);
	        ExtentReportMetadata.put("Browser",               browser);
	        ExtentReportMetadata.put("Browser Version",       browserVersion);
	        ExtentReportMetadata.put("Downloads Directory",   projectDir + "\\downloads");
	        ExtentReportMetadata.put("Environment",           env.toUpperCase());
	        ExtentReportMetadata.put("Execution URL",         baseUrl);
	        ExtentReportMetadata.put("Logs Directory",        projectDir + "\\logs");
	        ExtentReportMetadata.put("OS Version",            getOsVersion());
	        ExtentReportMetadata.put("Reports Directory",     projectDir + "\\extent-reports");
	        ExtentReportMetadata.put("Screenshots Directory", projectDir + "\\extent-reports\\screenshots");

	        // Add user + tenant info per role
	        addUserInfo();

	        ExtentReportMetadata.publishOnce();

	        System.out.println("========================================");
	    }

	    @AfterAll
	    public static void afterAll() {
	        System.out.println("========================================");
	        System.out.println("🏁 FRAMEWORK SHUTDOWN");
	        System.out.println("========================================");

	        SessionManager.closeAllRoleContexts();
	        BrowserManager.shutdown();

	        System.out.println("ℹ️  Session files preserved for session reuse");
	        System.out.println("   Location: sessions/" + PropertiesLoader.getBuildId() + "/");
	    }

	    /**
	     * Get browser version from Playwright.
	     */
	    private static String getBrowserVersion(String browserName) {
	        try {
	            return browserName + " " + BrowserManager.getBrowser().version();
	        } catch (Exception e) {
	            return "Unknown";
	        }
	    }

	    /**
	     * Get OS version.
	     */
	    private static String getOsVersion() {
	        String osName = System.getProperty("os.name", "Unknown");
	        String osVersion = System.getProperty("os.version", "");

	        if (osName.toLowerCase().contains("windows")) {
	            try {
	                double version = Double.parseDouble(osVersion);
	                if (version >= 10.0) {
	                    return "Windows 10/11";
	                }
	            } catch (Exception e) {
	                // Ignore
	            }
	            return osName;
	        }

	        return osName + " " + osVersion;
	    }

	    /**
	     * Add user + tenant info per role to Extent Report.
	     *
	     * Output format:
	     * User [Role: creator]          = Validation17@sitero.com
	     * User [Role: creator] (Tenant) = VAL51
	     * User [Role: editor]           = Validation08@sitero.com
	     */
	    private static void addUserInfo() {
	        String[] roles = {"creator", "editor", "viewer", "deletor"};

	        for (String role : roles) {
	            try {
	                // Get username
	                String username = PropertiesLoader.getUsername(role);
	                if (username != null && !username.isBlank()) {
	                    ExtentReportMetadata.put(
	                        "User [Role: " + role + "]",
	                        username
	                    );
	                }

	                // Get tenant (if multi-tenant)
	                String tenant = PropertiesLoader.getTenantForRole(role);
	                if (tenant != null && !tenant.isBlank()) {
	                    ExtentReportMetadata.put(
	                        "User [Role: " + role + "] (Tenant)",
	                        tenant
	                    );
	                }

	            } catch (Exception e) {
	                // Role not configured - skip silently
	            }
	        }
	    }
	}