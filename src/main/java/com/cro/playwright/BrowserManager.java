package com.cro.playwright;

import com.microsoft.playwright.*;

public class BrowserManager {

	private static Playwright playwright;
    private static Browser browser;

    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private BrowserManager() {}

    public static synchronized void initBrowser(String browserName) {
        if (browser != null) {
            System.out.println("⚠ Browser already initialized, skipping...");
            return;
        }

        playwright = Playwright.create();

        BrowserType type;
        switch (browserName.toLowerCase()) {
            case "firefox":
                type = playwright.firefox();
                break;
            case "webkit":
                type = playwright.webkit();
                break;
            case "chromium":
            default:
                type = playwright.chromium();
        }

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "true"));

        browser = type.launch(new BrowserType.LaunchOptions()
                .setHeadless(headless));

        System.out.println("✓ Browser launched: " + browserName + " (headless=" + headless + ")");
    }

    public static Browser getBrowser() {
        if (browser == null) {
            throw new IllegalStateException("Browser not initialized. Call initBrowser() first.");
        }
        return browser;
    }

    public static void setContext(BrowserContext context) {
        contextThreadLocal.set(context);
        pageThreadLocal.set(context.newPage());
    }

    public static BrowserContext getContext() {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx == null) {
            throw new IllegalStateException("BrowserContext not set for this thread");
        }
        return ctx;
    }

    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new IllegalStateException("Page not set for this thread");
        }
        return page;
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
            System.out.println("✓ Browser shutdown complete");
        }
    }
}