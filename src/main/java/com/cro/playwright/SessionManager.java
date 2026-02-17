package com.cro.playwright;

import com.cro.config.PropertiesLoader;
import com.microsoft.playwright.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session manager - SEQUENTIAL execution.
 * ONE context per ROLE, reused across scenarios.
 */
public final class SessionManager {

    private static final int MAX_LOGIN_RETRIES = 2;

    // Store contexts per role for reuse across scenarios
    private static final Map<String, BrowserContext> ROLE_CONTEXTS
            = new ConcurrentHashMap<>();

    private SessionManager() {}

    @FunctionalInterface
    public interface LoginCallback {
        void performLogin(Page page);
    }

    /**
     * Create or reuse session for a role.
     */
    public static void createOrReuseSession(String role,
                                            LoginCallback loginCallback) {

        String username = PropertiesLoader.getUsername(role);
        String buildId  = PropertiesLoader.getBuildId();

        String projectRoot = System.getProperty("user.dir");
        String sessionDir  = projectRoot + File.separator
                           + "sessions" + File.separator + buildId;
        String fileName    = sessionDir + File.separator
                           + role + "_" + username + ".json";
        Path path = Paths.get(fileName);

        System.out.println("📂 Session directory: " + sessionDir);
        System.out.println("📄 Session file: " + path.getFileName());

        try {
            Files.createDirectories(Paths.get(sessionDir));
            System.out.println("✓ Directory created/verified");
        } catch (Exception e) {
            throw new RuntimeException(
                "Unable to create session directory: " + sessionDir, e);
        }

        // Reuse existing context for this role if already created
        BrowserContext context = ROLE_CONTEXTS.get(role);

        if (context != null) {
            System.out.println("✓ Reusing existing context for role: " + role);
            BrowserManager.setContext(context);
            return;
        }

        Browser browser = BrowserManager.getBrowser();

        if (Files.exists(path)) {
            System.out.println("✓ Reusing session file: " + path.getFileName());

            try {
                context = browser.newContext(
                        new Browser.NewContextOptions()
                                .setStorageStatePath(path)
                                .setViewportSize(null));
            } catch (Exception e) {
                System.err.println("⚠ Session file corrupted: " + e.getMessage());
                try { Files.delete(path); } catch (Exception ignore) {}
                context = createNewSession(browser, loginCallback, path, username);
            }
        } else {
            context = createNewSession(browser, loginCallback, path, username);
        }

        // Store for reuse across scenarios
        ROLE_CONTEXTS.put(role, context);
        BrowserManager.setContext(context);

        System.out.println("✓ Context ready for: " +
            Thread.currentThread().getName());
    }

    /**
     * Create new session with login.
     */
    private static BrowserContext createNewSession(Browser browser,
                                                   LoginCallback loginCallback,
                                                   Path path,
                                                   String username) {
        System.out.println("🔐 Creating NEW session for: " + username);

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(null));

        Page page = context.newPage();

        int attempt = 1;
        Exception lastException = null;

        while (attempt <= MAX_LOGIN_RETRIES) {
            try {
                System.out.println("  Login attempt " + attempt
                        + "/" + MAX_LOGIN_RETRIES);

                loginCallback.performLogin(page);

                context.storageState(
                        new BrowserContext.StorageStateOptions()
                                .setPath(path));

                if (Files.exists(path)) {
                    long size = Files.size(path);
                    System.out.println("✅ Session saved: " + path.getFileName());
                    System.out.println("   Size: " + size + " bytes");

                    if (size < 50) {
                        throw new RuntimeException(
                            "Session file too small - login may have failed");
                    }
                } else {
                    throw new RuntimeException("Session file not created");
                }

                page.close();
                return context;

            } catch (Exception e) {
                lastException = e;
                System.err.println("  ❌ Attempt " + attempt
                        + " failed: " + e.getMessage());

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

        page.close();
        context.close();
        throw new RuntimeException(
            "Failed to create session after "
            + MAX_LOGIN_RETRIES + " attempts", lastException);
    }

    /**
     * Close all role contexts at @AfterAll.
     */
    public static synchronized void closeAllRoleContexts() {
        System.out.println("Closing " + ROLE_CONTEXTS.size()
                + " role context(s)...");

        ROLE_CONTEXTS.forEach((role, ctx) -> {
            try {
                ctx.close();
                System.out.println("  ✓ Closed context for role: " + role);
            } catch (Exception e) {
                System.err.println("  ⚠ Error closing context for role "
                        + role + ": " + e.getMessage());
            }
        });

        ROLE_CONTEXTS.clear();
        System.out.println("✓ All role contexts closed");
    }
}