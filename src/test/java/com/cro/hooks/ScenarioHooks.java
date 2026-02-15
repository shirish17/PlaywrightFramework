package com.cro.hooks;

import com.cro.context.RoleContext;
import com.cro.playwright.BrowserManager;
import com.cro.playwright.SessionManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {

	@Before
    public void setup(Scenario scenario) {
        RoleContext.clear();
        
        System.out.println("\n▶ Starting: " + scenario.getName());
        System.out.println("  Thread: " + Thread.currentThread().getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        
        try {
            if (scenario.isFailed()) {
                System.out.println("  ❌ Scenario FAILED");
                
                try {
                    byte[] screenshot = BrowserManager.getPage().screenshot();
                    scenario.attach(screenshot, "image/png", "Failure Screenshot");
                } catch (Exception e) {
                    System.err.println("  ⚠ Screenshot failed: " + e.getMessage());
                }
            } else {
                System.out.println("  ✅ Scenario PASSED");
            }
        } finally {
            // SAFE CLOSE: Uses ReadWriteLock to wait for any context creation
            BrowserManager.closeContext();
            RoleContext.clear();
        }
    }
}