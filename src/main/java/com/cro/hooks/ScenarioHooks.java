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
        String role = RoleContext.getRole();
        SessionManager.createOrReuse(role);
    }

    @After
    public void tearDown(Scenario scenario) {

        if (scenario.isFailed()) {
            byte[] screenshot = BrowserManager.getPage()
                    .screenshot();
            scenario.attach(screenshot, "image/png", "Failure Screenshot");
        }

        BrowserManager.closeContext();
        RoleContext.clear();
    }
}
