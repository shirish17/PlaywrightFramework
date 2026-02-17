package com.cro.playwright;

import com.microsoft.playwright.*;

import java.util.List;

/**
 * Browser lifecycle manager - SEQUENTIAL execution.
 * Singleton Playwright, Browser, Context and Page.
 */
public class BrowserManager {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private BrowserManager() {}

    /**
     * Initialize browser once at @BeforeAll.
     */
    public static synchronized void initBrowser(String browserType) {
        if (browser != null && browser.isConnected()) {
            System.out.println("⚠ Browser already initialized");
            return;
        }

        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "false"));

        String normalizedBrowser = (browserType == null || browserType.isBlank())
                ? "chrome"
                : browserType.toLowerCase().trim();

        switch (normalizedBrowser) {

            case "chrome":
                browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(headless)
                                .setChannel("chrome")
                                .setArgs(List.of("--start-maximized"))
                );
                break;

            case "edge":
            case "msedge":
                browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(headless)
                                .setChannel("msedge")
                                .setArgs(List.of("--start-maximized"))
                );
                break;

            case "chromium":
                browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(headless)
                                .setArgs(List.of("--start-maximized"))
                );
                break;

            case "firefox":
                browser = playwright.firefox().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(headless)
                );
                break;

            case "webkit":
            case "safari":
                browser = playwright.webkit().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(headless)
                );
                break;

            default:
                playwright.close();
                throw new IllegalArgumentException(
                    "\n╔════════════════════════════════════════════════════════╗\n" +
                    "║  UNSUPPORTED BROWSER: " + browserType + "              \n" +
                    "║                                                        ║\n" +
                    "║  Supported values (case-insensitive):                  ║\n" +
                    "║  chrome, chromium, firefox, webkit, safari,            ║\n" +
                    "║  edge, msedge                                          ║\n" +
                    "╚════════════════════════════════════════════════════════╝"
                );
        }

        System.out.println("✓ Browser launched: " + normalizedBrowser +
                " (headless=" + headless + ")");
    }

    /**
     * Get browser instance.
     */
    public static Browser getBrowser() {
        if (browser == null) {
            throw new IllegalStateException("Browser not initialized");
        }
        if (!browser.isConnected()) {
            throw new IllegalStateException("Browser disconnected");
        }
        return browser;
    }

    /**
     * Set context and create page.
     * setViewportSize(null) = real maximized window.
     */
    public static void setContext(BrowserContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Cannot set null context");
        }
        context = ctx;

        // Reuse existing page if already open (avoids creating duplicate pages)
        // Session reuse path: context has no pages → create one
        // New session path: login page was closed → create one
        // Either way context.pages() will be empty, so newPage() is correct
        if (!ctx.pages().isEmpty()) {
            // Context already has an open page - reuse it
            page = ctx.pages().get(0);
            System.out.println("  ✓ Reusing existing page in context");
        } else {
            // No open pages - create fresh one
            page = ctx.newPage();
            System.out.println("  ✓ New page created for context");
        }

        System.out.println("  ✓ Context set for thread: " +
            Thread.currentThread().getName());
    }

    /**
     * Get current context.
     */
    public static BrowserContext getContext() {
        if (context == null) {
            throw new IllegalStateException("Context not set");
        }
        return context;
    }

    /**
     * Get current page.
     */
    public static Page getPage() {
        if (page == null) {
            throw new IllegalStateException("Page not set");
        }
        if (page.isClosed()) {
            throw new IllegalStateException("Page is closed");
        }
        return page;
    }

    /**
     * Close current context.
     * Called from @After in ScenarioHooks.
     */
    public static void closeContext() {
        if (context == null) {
            context = null;
            page = null;
            return;
        }

        try {
            context.close();
            System.out.println("  ✓ Context closed");
        } catch (Exception e) {
            System.err.println("  ⚠ Error closing context: " + e.getMessage());
        } finally {
            context = null;
            page = null;
        }
    }

    /**
     * Shutdown browser at @AfterAll.
     */
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
            playwright = null;
            System.out.println("✓ Browser shutdown complete");
        }
    }
}