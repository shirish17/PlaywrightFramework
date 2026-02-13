package com.cro.hooks;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class GlobalHooks {

    @BeforeAll
    public static void beforeAll() {
        PropertiesLoader.load();
        BrowserManager.initBrowser(PropertiesLoader.getBrowser());
    }

    @AfterAll
    public static void afterAll() {
        BrowserManager.shutdown();
    }
}
