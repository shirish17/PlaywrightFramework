package com.cro.playwright;

import com.cro.config.PropertiesLoader;
import com.microsoft.playwright.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SessionManager {

    private static final Object LOCK = new Object();

    private SessionManager() {}

    /**
     * Creates or reuses session per buildId + role + username.
     * This ensures:
     * - 20 unique users = 20 logins only (per buildId)
     * - Session is isolated per test run (buildId)
     * - Thread-safe session creation
     */
    public static void createOrReuseSession(String role) {

        String username = PropertiesLoader.getUsername(role);
        String password = PropertiesLoader.getPassword(role);

        // buildId ensures session isolation per test run
        String buildId = PropertiesLoader.getBuildId();
        String sessionDir = "sessions/" + buildId;
        String fileName = sessionDir + "/" + role + "_" + username + ".json";
        Path path = Paths.get(fileName);
        
     // 🔍 DEBUG: Print absolute path
        File absoluteFile = path.toAbsolutePath().toFile();
        System.out.println("   DEBUG Session Info:");
        System.out.println("   Build ID: " + buildId);
        System.out.println("   Session Dir: " + sessionDir);
        System.out.println("   Filename: " + fileName);
        System.out.println("   Absolute Path: " + absoluteFile.getAbsolutePath());
        System.out.println("   File exists: " + Files.exists(path));
        System.out.println("   Parent dir exists: " + Files.exists(path.getParent()));

        try {
            Files.createDirectories(Paths.get(sessionDir));
        } catch (Exception e) {
            throw new RuntimeException("Unable to create session directory: " + sessionDir, e);
        }

        Browser browser = BrowserManager.getBrowser();
        BrowserContext context;

        if (Files.exists(path)) {
            System.out.println("Reusing session: " + fileName);
            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(path));
        } else {
            synchronized (LOCK) {
                // Double-check inside lock
                if (Files.exists(path)) {
                    System.out.println("Reusing session (race avoided): " + fileName);
                    context = browser.newContext(
                            new Browser.NewContextOptions()
                                    .setStorageStatePath(path));
                } else {
                    System.out.println("Creating NEW session: " + fileName);
                    context = browser.newContext();
                    Page page = context.newPage();

                    try {
                        page.navigate(PropertiesLoader.getBaseUrl());

                        // TODO: Replace with real login selectors
                        page.fill("#username", username);
                        page.fill("#password", password);
                        page.click("#loginBtn");
                        page.waitForLoadState();

                        context.storageState(
                                new BrowserContext.StorageStateOptions()
                                        .setPath(path));

                        System.out.println("Session saved: " + fileName);
                    } finally {
                        page.close(); // CRITICAL: Close temporary login page
                    }
                }
            }
        }

        BrowserManager.setContext(context);
    }
}