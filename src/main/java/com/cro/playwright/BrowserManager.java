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

    private static final ReadWriteLock contextLock = new ReentrantReadWriteLock();

    private BrowserManager() {}

    public static synchronized void initBrowser(String browserName) {

        if (browser != null && browser.isConnected())
            return;

        playwright = Playwright.create();

        BrowserType type;
        switch (browserName.toLowerCase()) {
            case "firefox": type = playwright.firefox(); break;
            case "webkit": type = playwright.webkit(); break;
            default: type = playwright.chromium();
        }

        boolean headless = Boolean.parseBoolean(System.getProperty("headless","true"));

        browser = type.launch(new BrowserType.LaunchOptions().setHeadless(headless));

        System.out.println("✓ Browser launched: " + browserName);
    }

    public static Browser getBrowser() {

        if (browser == null || !browser.isConnected()) {
            System.out.println("⚠ Browser disconnected. Relaunching...");
            initBrowser("chromium");
        }
        return browser;
    }

    public static void acquireContextCreationLock() {
        contextLock.readLock().lock();
    }

    public static void releaseContextCreationLock() {
        contextLock.readLock().unlock();
    }

    public static void setContext(BrowserContext context) {

        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);
    }

    public static Page getPage() {
        return pageThreadLocal.get();
    }

    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    public static void closeContext() {

        BrowserContext ctx = contextThreadLocal.get();
        if (ctx == null) return;

        contextLock.writeLock().lock();
        try {
            ctx.close();
        } finally {
            contextThreadLocal.remove();
            pageThreadLocal.remove();
            contextLock.writeLock().unlock();
        }
    }

    public static synchronized void shutdown() {

        if (browser == null) return;

        try { browser.close(); } catch(Exception ignored){}
        try { playwright.close(); } catch(Exception ignored){}

        browser=null;
        System.out.println("✓ Browser shutdown");
    }
}
