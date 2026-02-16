package com.cro.pages;

import com.cro.context.RoleContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Home page - handles post-login operations.
 * CRITICAL: Handles multi-tenant selection after session reuse.
 * Once your land on this page, it may or may not show mutli-tenant pop-up depending on user configuration
 */
public class HomePage extends BasePage {
	 private static final String TENANT_POPUP = "#showMultiTenantPopup";

	    /**
	     * Handle tenant selection if popup appears.
	     */
	    public HomePage handleTenantSelection() {
	        String role = RoleContext.getRole();
	        
	        // Wait for page to be ready
	        waitForHomePage();
	        
	        // Handle tenant popup
	        handleTenantSelectionForRole(TENANT_POPUP, role);
	        
	        return this;
	    }

	    /**
	     * Wait for home page to be ready.
	     */
	    private void waitForHomePage() {
	        System.out.println("  Waiting for home page to load...");
	        
	        // Wait for Kendo/Angular
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