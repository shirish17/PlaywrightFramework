package com.cro.playwright;

import com.cro.config.PropertiesLoader;
import com.microsoft.playwright.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages Playwright session storage for authentication state reuse.
 * 
 * CRITICAL: Thread-safe session creation using synchronized block.
 * Uses ONLY raw Playwright API - NO page objects here.
 * Delegates actual login to a LoginCallback - maintains separation of concerns.
 */
public final class SessionManager {

    // CRITICAL: Synchronize ALL browser operations
    private static final Object BROWSER_LOCK = new Object();
    private static final int MAX_LOGIN_RETRIES = 2;

    private SessionManager() {}

    @FunctionalInterface
    public interface LoginCallback {
        void performLogin(Page page);
    }

    /**
     * Create or reuse session with complete thread-safety.
     * 
     * CRITICAL: ALL browser.newContext() calls are synchronized.
     */
    public static void createOrReuseSession(String role, LoginCallback loginCallback) {

        String username = PropertiesLoader.getUsername(role);
        String buildId = PropertiesLoader.getBuildId();

        String projectRoot = System.getProperty("user.dir");
        String sessionDir = projectRoot + File.separator + "sessions" + File.separator + buildId;
        String fileName = sessionDir + File.separator + role + "_" + username + ".json";
        Path path = Paths.get(fileName);

        System.out.println("📂 Session directory: " + sessionDir);
        System.out.println("📄 Session file: " + path.toAbsolutePath());

        try {
            Files.createDirectories(Paths.get(sessionDir));
            System.out.println("✓ Directory created/verified");
        } catch (Exception e) {
            throw new RuntimeException("Unable to create session directory: " + sessionDir, e);
        }

        BrowserContext context;

        // ✅ CRITICAL: SYNCHRONIZE ALL CONTEXT OPERATIONS
        synchronized (BROWSER_LOCK) {
            Browser browser = BrowserManager.getBrowser();

            if (Files.exists(path)) {
                System.out.println("✓ Reusing session: " + path.getFileName());
                
                try {
                    context = browser.newContext(
                            new Browser.NewContextOptions()
                                    .setStorageStatePath(path));
                } catch (Exception e) {
                    System.err.println("⚠ Session file corrupted: " + e.getMessage());
                    try {
                        Files.delete(path);
                    } catch (Exception deleteError) {
                        // Ignore delete errors
                    }
                    context = createNewSession(browser, loginCallback, path, username);
                }
            } else {
                context = createNewSession(browser, loginCallback, path, username);
            }

            // CRITICAL: Set context INSIDE synchronized block
            BrowserManager.setContext(context);
            
        } // Lock released here
        
        // CRITICAL: Release context protection AFTER lock exits
        BrowserManager.releaseContext();
        
        System.out.println("✓ Context ready for thread: " + Thread.currentThread().getName());
    }

    /**
     * Create new session with retry logic.
     * 
     * IMPORTANT: This method is called from inside synchronized block.
     */
    private static BrowserContext createNewSession(Browser browser, 
                                                   LoginCallback loginCallback, 
                                                   Path path, 
                                                   String username) {
        System.out.println("🔐 Creating NEW session for: " + username);

        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        int attempt = 1;
        Exception lastException = null;

        while (attempt <= MAX_LOGIN_RETRIES) {
            try {
                System.out.println("  Login attempt " + attempt + "/" + MAX_LOGIN_RETRIES);
                
                loginCallback.performLogin(page);

                context.storageState(
                        new BrowserContext.StorageStateOptions()
                                .setPath(path));

                if (Files.exists(path)) {
                    long fileSize = Files.size(path);
                    System.out.println("✅ Session saved: " + path.getFileName());
                    System.out.println("   Size: " + fileSize + " bytes");

                    if (fileSize < 50) {
                        throw new RuntimeException("Session file too small - login failed");
                    }
                } else {
                    throw new RuntimeException("Session file not created");
                }

                // Success!
                page.close();
                return context;

            } catch (Exception e) {
                lastException = e;
                System.err.println("  ❌ Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < MAX_LOGIN_RETRIES) {
                    System.out.println("  Retrying in 2 seconds...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                attempt++;
            }
        }

        // All retries failed
        page.close();
        context.close();
        throw new RuntimeException("Failed to create session after " + MAX_LOGIN_RETRIES + 
                                 " attempts", lastException);
    }
}