package com.cro.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Login page object.
 */
public class LoginPage extends BasePage {

    private static final String ADFS_ACTIVE_DIRECTORY = "span.largeTextNoWrap:has-text('Active Directory')";
    private static final String USERNAME = "#userNameInput";
    private static final String PASSWORD = "#passwordInput"; 
    private static final String SIGNIN_PARENT_BY_ID = "#submissionArea";
    private static final String SIGNIN_BUTTON_BY_TEXT = "Sign in";

    // ========== LOCATORS ==========
    
    private Locator adfsButton() {
        return locator(ADFS_ACTIVE_DIRECTORY);
    }

    private Locator usernameInput() {
        return locator(USERNAME);
    }

    private Locator passwordInput() {
        return locator(PASSWORD);
    }
    
    private Locator loginButton() {
        return getByRoleWithinParent(
            locator(SIGNIN_PARENT_BY_ID),
            AriaRole.BUTTON,
            SIGNIN_BUTTON_BY_TEXT);
    }

    // ========== ACTIONS ==========

    public LoginPage open(String baseUrl) {
        navigate(baseUrl);
        waitForLoadState();
        return this;
    }

    public void login(String username, String password) {
        System.out.println("   🔐 Logging in as: " + username);

        // Wait for ADFS page to fully render, then click Active Directory
        try {
            waitForVisible(adfsButton());
            clickOnElement(adfsButton());
            waitForLoadState();
        } catch (Exception e) {
            System.out.println("   No ADFS screen - proceeding directly");
        }

        fillElement(usernameInput(), username);
        fillElement(passwordInput(), password);
        clickOnElement(loginButton());

        waitForLoadState();

        try {
            getPage().waitForTimeout(3000);
        } catch (Exception ignored) {}

        System.out.println("Login completed");
    }
    public static void performLoginForSession(Page page, String username, 
                                              String password, String baseUrl) {
        LoginPage loginPage = new LoginPage() {
            @Override
            protected Page getPage() {
                return page;
            }
        };
        
        loginPage.navigate(baseUrl);
        loginPage.login(username, password);
    }

    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("login") || isVisible(loginButton());
    }
}