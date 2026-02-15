package com.cro.playwright;

import com.microsoft.playwright.*;

public class BrowserManager {

    private static Playwright playwright;
    private static Browser browser;

    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    
    // CRITICAL: Track if context is in use by synchronized block
    private static final ThreadLocal<Boolean> contextInUse = new ThreadLocal<>();

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
            throw new IllegalStateException("Browser disconnected");
        }
        
        return browser;
    }

    /**
     * Set context and mark as in-use.
     * Called from SessionManager inside synchronized block.
     */
    public static void setContext(BrowserContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Cannot set null context");
        }
        
        // Mark context as in-use (protected from early closure)
        contextInUse.set(true);
        
        contextThreadLocal.set(context);
        pageThreadLocal.set(context.newPage());
        
        System.out.println("  ✓ Context set for: " + Thread.currentThread().getName());
    }

    /**
     * Mark context as ready for cleanup.
     * Called from SessionManager after synchronized block exits.
     */
    public static void releaseContext() {
        contextInUse.set(false);
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

    /**
     * Safe close - only close if not in use by synchronized block.
     * Called from @After hook.
     */
    public static void safeCloseContext() {
        Boolean inUse = contextInUse.get();
        if (inUse != null && inUse) {
            // Context still in use by SessionManager - DON'T close
            System.out.println("  ⚠ Context in use - skipping close");
            return;
        }
        
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx != null) {
            try {
                ctx.close();
                System.out.println("  ✓ Context closed");
            } catch (Exception e) {
                System.err.println("  ⚠ Error closing context: " + e.getMessage());
            } finally {
                contextThreadLocal.remove();
                pageThreadLocal.remove();
                contextInUse.remove();
            }
        } else {
            // Still clean ThreadLocal
            contextThreadLocal.remove();
            pageThreadLocal.remove();
            contextInUse.remove();
        }
    }

    /**
     * Clear ThreadLocal without closing context.
     * Used in @Before hook for defensive cleanup.
     */
    public static void clearThreadLocal() {
        contextThreadLocal.remove();
        pageThreadLocal.remove();
        contextInUse.remove();
    }

    /**
     * Force close - backward compatibility.
     * Prefer safeCloseContext() instead.
     */
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
                contextInUse.remove();
            }
        } else {
            contextThreadLocal.remove();
            pageThreadLocal.remove();
            contextInUse.remove();
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