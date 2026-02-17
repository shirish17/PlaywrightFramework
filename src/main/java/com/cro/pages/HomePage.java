package com.cro.pages;

import com.cro.context.RoleContext;

/**
 * Home page - handles post-login operations.
 */
public class HomePage extends BasePage {

    private static final String TENANT_POPUP = "#showMultiTenantPopup";

    /**
     * Handle tenant selection if popup appears.
     */
    public HomePage handleTenantSelection() {
        String role = RoleContext.getRole();
        
        waitForHomePage();
        handleTenantSelectionForRole(TENANT_POPUP, role);
        
        return this;
    }

    /**
     * Wait for home page to be ready.
     */
    private void waitForHomePage() {
        System.out.println("  Waiting for home page to load...");
        waitForKendoAngularPageReady();
        System.out.println("  ✓ Home page ready");
    }

    /**
     * Verify we're on home page.
     */
    public boolean isOnHomePage() {
        return !getCurrentUrl().contains("login");
    }
}