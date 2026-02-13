package com.cro.playwright;

import com.cro.config.PropertiesLoader;
import com.microsoft.playwright.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SessionManager {

    private static final Object LOCK = new Object();

    private SessionManager() {}

    public static void createOrReuse(String role) {

        String username = PropertiesLoader.getUsername(role);
        String password = PropertiesLoader.getPassword(role);

        String fileName = "sessions/" + role + "_" + username + ".json";
        Path path = Paths.get(fileName);

        try {
            Files.createDirectories(Paths.get("sessions"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to create session directory", e);
        }

        Browser browser = BrowserManager.getBrowser();
        BrowserContext context;

        if (Files.exists(path)) {
            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(path));
        } else {

            synchronized (LOCK) {

                if (Files.exists(path)) {
                    context = browser.newContext(
                            new Browser.NewContextOptions()
                                    .setStorageStatePath(path));
                } else {

                    context = browser.newContext();
                    Page page = context.newPage();
                    page.navigate(PropertiesLoader.getBaseUrl());

                    // TODO: Replace with real login steps
                    page.fill("#username", username);
                    page.fill("#password", password);
                    page.click("#loginBtn");
                    page.waitForLoadState();

                    context.storageState(
                            new BrowserContext.StorageStateOptions()
                                    .setPath(path));
                }
            }
        }

        BrowserManager.setContext(context);
    }
}
