package com.cro.hooks;

import com.cro.context.RoleContext;
import com.cro.playwright.BrowserManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Scenario-level hooks.
 * Screenshots attached via Cucumber's scenario.attach() - picked up by Extent Adapter automatically.
 */
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
                    // Attach screenshot - Extent Adapter picks it up automatically
                    byte[] screenshot = BrowserManager.getPage().screenshot();
                    scenario.attach(screenshot, "image/png", scenario.getName());
                    System.out.println("  📸 Screenshot attached");
                } catch (Exception e) {
                    System.err.println("  ⚠ Screenshot failed: " + e.getMessage());
                }
            } else {
                System.out.println("  ✅ Scenario PASSED");
            }
        } finally {
            RoleContext.clear();
        }
    }
}