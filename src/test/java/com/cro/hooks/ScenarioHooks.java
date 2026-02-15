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
            // Defensive cleanup - clear any stale ThreadLocal
            BrowserManager.clearThreadLocal();
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
                // CRITICAL: Safe cleanup - respects contextInUse flag
                BrowserManager.safeCloseContext();
                RoleContext.clear();
            }
        }
    }