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

    private static final Object LOCK = new Object();

    private SessionManager() {}
    
    /**
     * Functional interface for login delegation.
     * Allows SessionManager to trigger login without knowing HOW to login.
     */
    @FunctionalInterface
    public interface LoginCallback {
        /**
         * Perform login using the provided page.
         * @param page Playwright Page to use for login
         */
        void performLogin(Page page);
    }

    /**
     * Create or reuse session for the given role.
     * Thread-safe: Each thread gets its own context and page.
     */
    public static void createOrReuseSession(String role, LoginCallback loginCallback) {

        String username = PropertiesLoader.getUsername(role); 
        String buildId = PropertiesLoader.getBuildId();
        
     // Use project root explicitly, sometimes relative path is misinterpreted by system
        String projectRoot = System.getProperty("user.dir");
        String sessionDir = projectRoot + File.separator + "sessions" + File.separator + buildId;
        String fileName = sessionDir + File.separator + role + "_" + username + ".json";
        Path path = Paths.get(fileName);
     // 🔍 DEBUG: Log absolute path to see where the json files are getting stored
        System.out.println("📂 Session directory: " + sessionDir);
        System.out.println("📄 Session file: " + path.toAbsolutePath());

        try {
            Files.createDirectories(Paths.get(sessionDir));
            System.out.println("✓ Directory created/verified: " + sessionDir);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create session directory: " + sessionDir, e);
        }

        Browser browser = BrowserManager.getBrowser();
        BrowserContext context;

        if (Files.exists(path)) {
            System.out.println("✓ Reusing session: " + path.getFileName());
            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(path));
        }  else {
            // CREATE NEW SESSION
            synchronized (LOCK) {
                // Double-check pattern
                if (Files.exists(path)) {
                    System.out.println("Reusing session (race avoided): " + path.getFileName());
                    context = browser.newContext(
                            new Browser.NewContextOptions()
                                    .setStorageStatePath(path));
                } else {
                    System.out.println("Creating NEW session for: " + role);                    
                    // Create fresh context
                    context = browser.newContext();
                    Page page = context.newPage();

                    try {
                    	// DELEGATE LOGIN TO CALLBACK (page object layer)
                        loginCallback.performLogin(page);                        
                        // Save session
                        context.storageState(
                                new BrowserContext.StorageStateOptions()
                                        .setPath(path));

                     // ✅ VERIFY FILE WAS CREATED
                        if (Files.exists(path)) {
                            long fileSize = Files.size(path);
                            System.out.println("✅ Session saved: " + path.getFileName());
                            System.out.println("   Location: " + path.toAbsolutePath());
                            System.out.println("   Size: " + fileSize + " bytes");

                            if (fileSize < 50) {
                                System.err.println("⚠️  WARNING: Session file is very small - login may have failed!");
                            }
                        } else {
                            System.err.println("❌ CRITICAL: Session file NOT created!");
                            System.err.println("   Expected at: " + path.toAbsolutePath());
                        }

                    } catch (Exception e) {
                        System.err.println("Session creation failed:");
                        e.printStackTrace();
                        throw new RuntimeException("Failed to create session for " + role, e);
                    } finally {
                        // CRITICAL: Close temporary login page
                        page.close();
                    }
                }
            }
        }

        // CRITICAL: Set context for current thread
        // This creates the Page that tests will use
        BrowserManager.setContext(context);
    }
}