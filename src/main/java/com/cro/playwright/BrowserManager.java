package com.cro.playwright;

import com.microsoft.playwright.*;

public class BrowserManager {


    private static Playwright playwright;
    private static Browser browser;

    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private BrowserManager() {}

    public static synchronized void initBrowser(String browserName) {
        if (browser != null && browser.isConnected()) {
            System.out.println("⚠ Browser already initialized");
            return;
        }

        if (browser != null && !browser.isConnected()) {
            System.err.println("⚠ Browser disconnected - reinitializing");
            shutdown();
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
            throw new IllegalStateException("Browser not initialized");
        }
        
        if (!browser.isConnected()) {
            throw new IllegalStateException("Browser disconnected - restart needed");
        }
        
        return browser;
    }

    public static void setContext(BrowserContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Cannot set null context");
        }
        
        // Defensive: clean existing context if any
        BrowserContext existing = contextThreadLocal.get();
        if (existing != null && existing != context) {
            try {
                existing.close();
            } catch (Exception e) {
                // Ignore - new context will replace it anyway
            }
        }
        
        contextThreadLocal.set(context);
        pageThreadLocal.set(context.newPage());
        
        System.out.println("  ✓ Context set for: " + Thread.currentThread().getName());
    }

    public static BrowserContext getContext() {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx == null) {
            throw new IllegalStateException(
                "Context not set for thread: " + Thread.currentThread().getName());
        }
        return ctx;
    }

    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new IllegalStateException(
                "Page not set for thread: " + Thread.currentThread().getName());
        }
        
        if (page.isClosed()) {
            throw new IllegalStateException("Page is closed");
        }
        
        return page;
    }

    public static void closeContext() {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
                System.err.println("  ⚠ Error closing context: " + e.getMessage());
            } finally {
                contextThreadLocal.remove();
                pageThreadLocal.remove();
            }
        } else {
            // Still clean ThreadLocal
            contextThreadLocal.remove();
            pageThreadLocal.remove();
        }
    }

    public static synchronized void shutdown() {
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception e) {
                System.err.println("⚠ Error closing browser: " + e.getMessage());
            }
            
            try {
                playwright.close();
            } catch (Exception e) {
                System.err.println("⚠ Error closing playwright: " + e.getMessage());
            }
            
            browser = null;
            System.out.println("✓ Browser shutdown complete");
        }
    }
}