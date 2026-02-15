package com.cro.pages;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Login page object.
 * EXTENDS BasePage for direct access to actions.
 */

public class LoginPage extends BasePage {

    // ========== SELECTORS ==========
    // TODO: Replace with your actual selectors
	private static final String ADFS_ACTIVE_DIRECTORY="text=Active Directory";
	private static final String USERNAME = "#userNameInput";
    private static final String PASSWORD = "#passwordInput"; 
    private static final String SIGNIN_PARENT_BY_ID="#submissionArea";
    private static final String SIGNIN_BUTTON_BY_ID="#submitButton";
    private static final String SIGNIN_BUTTON_BY_TEXT="Sign in";

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
        return getByRoleWithinParent(locator(SIGNIN_PARENT_BY_ID),AriaRole.BUTTON,SIGNIN_BUTTON_BY_TEXT);
    }
	
    // ========== ACTIONS ==========

    /**
     * Navigate to login page.
     */
    public LoginPage open(String baseUrl) {
        navigate(baseUrl);
        waitForLoadState();
        return this;
    }

    /**
     * Perform login with credentials.
     * This is the method called by SessionManager via callback.
     */
    public void login(String username, String password) {
        System.out.println("   🔐 Logging in as: " + username);

        // Click ADFS if needed
        if (isVisible(adfsButton())) {
            clickOnElement(adfsButton());
            waitForLoadState();
        }

        fillElement(usernameInput(), username);
        fillElement(passwordInput(), password);
        clickOnElement(loginButton());
        waitForLoadState();

        System.out.println("Login completed");
    }
    /**
     * Static method for SessionManager callback.
     * Creates temporary LoginPage instance for session creation.
     */
    public static void performLoginForSession(Page page, String username, String password, String baseUrl) {
        // Temporarily set context so LoginPage can work
        // Note: This is a special case for session creation only
        BrowserContext tempContext = page.context();
        
        // Create temporary BasePage that wraps this page
        LoginPage loginPage = new LoginPage() {
            @Override
            protected Page getPage() {
                return page; // Override to use the session creation page
            }
        };

        loginPage.navigate(baseUrl);
        loginPage.login(username, password);
    }

    /**
     * Check if on login page.
     */
    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("login") || isVisible(loginButton());
    }
}