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
        
     // DEBUG: Check thread count
        String threads = System.getProperty("dp.threads", "NOT SET");
        System.out.println("Thread count (dp.threads): " + threads);
        
        PropertiesLoader.load();
        BrowserManager.initBrowser(PropertiesLoader.getBrowser());
        
        System.out.println("========================================");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("========================================");
        System.out.println("🏁 FRAMEWORK SHUTDOWN");
        System.out.println("========================================");
        
        BrowserManager.shutdown();
    }
}