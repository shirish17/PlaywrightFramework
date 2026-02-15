package com.cro.hooks;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class GlobalHooks {

	@BeforeAll
    public static void beforeAll() {
        System.out.println("========================================");
        System.out.println("🚀 FRAMEWORK INITIALIZATION");
        System.out.println("========================================");

        PropertiesLoader.load();
        
        String buildId = PropertiesLoader.getBuildId();
        String threads = System.getProperty("dp.threads", "1");
        System.out.println("Build ID: " + buildId);
        System.out.println("Threads: " + threads);
        
        BrowserManager.initBrowser(PropertiesLoader.getBrowser());

        System.out.println("========================================");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("========================================");
        System.out.println("🏁 FRAMEWORK SHUTDOWN");
        System.out.println("========================================");

        BrowserManager.shutdown();
        
        System.out.println("ℹ️  Session files preserved for session reuse");
        System.out.println("   Location: sessions/" + PropertiesLoader.getBuildId() + "/");
    }
}