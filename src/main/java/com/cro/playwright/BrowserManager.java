package com.cro.playwright;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
/**
 * Browser-Level Context Lifecycle Synchronization
 * 
 * Uses ReadWriteLock (Readers-Writer pattern) to coordinate:
 * - Context creation (multiple concurrent - read lock)
 * - Context closure (exclusive - write lock)
 * 
 * Prevents Playwright browser state corruption in parallel execution.
 */

public class BrowserManager {

	private static Playwright playwright;
    private static Browser browser;

    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    
    // ReadWriteLock: Multiple readers (context creation), single writer (context close)
    private static final ReadWriteLock contextLock = new ReentrantReadWriteLock();

    private BrowserManager() {}

    public static synchronized void initBrowser(String browserName) {
        if (browser != null && browser.isConnected()) {
            System.out.println("⚠ Browser already initialized");
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
            throw new IllegalStateException("Browser not initialized");
        }
        
        if (!browser.isConnected()) {
            throw new IllegalStateException("Browser disconnected");
        }
        
        return browser;
    }

    /**
     * Acquire read lock before context creation.
     * Prevents context closure during creation.
     */
    public static void acquireContextCreationLock() {
        contextLock.readLock().lock();
    }

    /**
     * Release read lock after context creation.
     */
    public static void releaseContextCreationLock() {
        contextLock.readLock().unlock();
    }

    public static void setContext(BrowserContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Cannot set null context");
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

    /**
     * Safe close context - acquires write lock to ensure no concurrent creation.
     */
    public static void closeContext() {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx == null) {
            contextThreadLocal.remove();
            pageThreadLocal.remove();
            return;
        }
        
        // Acquire write lock - blocks until all context creations complete
        contextLock.writeLock().lock();
        try {
            ctx.close();
            System.out.println("  ✓ Context closed safely");
        } catch (Exception e) {
            System.err.println("  ⚠ Error closing context: " + e.getMessage());
        } finally {
            contextThreadLocal.remove();
            pageThreadLocal.remove();
            contextLock.writeLock().unlock();
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