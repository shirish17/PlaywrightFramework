package com.cro.playwright;

import com.microsoft.playwright.*;

public class BrowserManager {

    private static Playwright playwright;
    private static Browser browser;

    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private BrowserManager() {}

    public static synchronized void initBrowser(String browserName) {
        if (browser != null) return;

        playwright = Playwright.create();

        BrowserType type;
        switch (browserName.toLowerCase()) {
            case "firefox":
                type = playwright.firefox();
                break;
            case "webkit":
                type = playwright.webkit();
                break;
            default:
                type = playwright.chromium();
        }

        browser = type.launch(new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(
                        System.getProperty("headless", "true"))));
    }

    public static Browser getBrowser() {
        return browser;
    }

    public static void setContext(BrowserContext context) {
        contextThreadLocal.set(context);
        pageThreadLocal.set(context.newPage());
    }

    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    public static Page getPage() {
        return pageThreadLocal.get();
    }

    public static void closeContext() {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx != null) {
            ctx.close();
            contextThreadLocal.remove();
            pageThreadLocal.remove();
        }
    }

    public static synchronized void shutdown() {
        if (browser != null) {
            browser.close();
            playwright.close();
            browser = null;
        }
    }
}
