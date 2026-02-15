package com.cro.hooks;

import com.cro.context.RoleContext;
import com.cro.playwright.BrowserManager;
import com.cro.playwright.SessionManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {
    /**
     * @Before hook - defensive ThreadLocal cleanup.
     * 
     * Why critical: Cucumber reuses threads in parallel mode.
     * If previous scenario crashed, ThreadLocal might have stale data.
     */
    @Before
    public void setup(Scenario scenario) {
        // Defensive cleanup - ensure no stale ThreadLocal
        try {
            BrowserManager.closeContext();
        } catch (Exception e) {
            // Ignore - context might not exist yet
        }
        
        RoleContext.clear();
        
        System.out.println("\n▶ Starting: " + scenario.getName());
        System.out.println("  Thread: " + Thread.currentThread().getName());
    }

    /**
     * @After hook - cleanup with exception handling.
     */
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
            // CRITICAL: Cleanup must happen even if screenshot fails
            try {
                BrowserManager.closeContext();
            } catch (Exception e) {
                System.err.println("  ⚠ Context cleanup failed: " + e.getMessage());
            }
            
            try {
                RoleContext.clear();
            } catch (Exception e) {
                System.err.println("  ⚠ Role cleanup failed: " + e.getMessage());
            }
        }
    }
}