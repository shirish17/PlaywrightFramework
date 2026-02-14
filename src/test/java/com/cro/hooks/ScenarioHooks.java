package com.cro.hooks;

import com.cro.context.RoleContext;
import com.cro.playwright.BrowserManager;
import com.cro.playwright.SessionManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class ScenarioHooks {

    // ❌ NO @Before - Session creation happens in Background step itself

    @After
    public void tearDown(Scenario scenario) {

        if (scenario.isFailed()) {
            System.out.println("  ❌ Scenario FAILED - capturing screenshot");
            
            try {
                byte[] screenshot = BrowserManager.getPage().screenshot();
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch (Exception e) {
                System.out.println("  ⚠ Screenshot capture failed: " + e.getMessage());
            }
        } else {
            System.out.println("  ✅ Scenario PASSED");
        }

        BrowserManager.closeContext();
        RoleContext.clear();
    }
}
