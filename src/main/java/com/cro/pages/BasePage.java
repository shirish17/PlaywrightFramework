package com.cro.pages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

/**
 * Base class for all page objects.
 * Thread-safe via ThreadLocal Page from BrowserManager.
 * Pages EXTEND this class to inherit common functionality.
 */
public class BasePage {
    protected final int elementTimeout;
    protected final int pageTimeout;

    /**
     * Constructor - loads timeouts from properties.
     */
    public BasePage() {
        this.elementTimeout = PropertiesLoader.getElementTimeout();
        this.pageTimeout = PropertiesLoader.getPageTimeout();
    }

    /**
     * Get the current thread's Page from BrowserManager.
     */
    protected Page getPage() {
        return BrowserManager.getPage();
    }

    // ========== LOCATOR CREATION ==========

    protected Locator locator(String selector) {
        return getPage().locator(selector);
    }
    
    //returns element which is tagged to page level
    protected Locator getByRole(AriaRole role, String name) {
        return getPage().getByRole(role, new Page.GetByRoleOptions().setName(name));
    }
    
 // overload method to get child available in parent using Role strategy (element tag to parent)
    protected Locator getByRoleWithinParent(Locator parent, AriaRole role, String childName) {
        return parent.getByRole(role, new Locator.GetByRoleOptions().setName(childName));
    }
    
    //Overloaded method to get child available in parent using string strategy (element tag to parent)
    public Locator getBywithinParent(Locator parent, String childSelector) {
		return parent.locator(childSelector);
	}

    protected Locator getByText(String text) {
        return getPage().getByText(text);
    }

    protected Locator getByPlaceholder(String placeholder) {
        return getPage().getByPlaceholder(placeholder);
    }

    protected Locator getByLabel(String label) {
        return getPage().getByLabel(label);
    }

    // ========== ACTIONS WITH TIMEOUT ==========

    public void clickOnElement(Locator locator) {
        locator.click(new Locator.ClickOptions().setTimeout(elementTimeout));
    }

    public void fillElement(Locator locator, String text) {
        locator.fill(text, new Locator.FillOptions().setTimeout(elementTimeout));
    }

    public void selectOption(Locator locator, String value) {
        locator.selectOption(value, new Locator.SelectOptionOptions().setTimeout(elementTimeout));
    }

    public String getTextContent(Locator locator) {
        return locator.textContent(new Locator.TextContentOptions().setTimeout(elementTimeout));
    }

    public boolean isVisible(Locator locator) {
        return locator.isVisible(new Locator.IsVisibleOptions().setTimeout(elementTimeout));
    }

    public void waitForVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(elementTimeout));
    }

    // ========== PAGE NAVIGATION ==========

    public void navigate(String url) {    	
        getPage().navigate(url, new Page
        		.NavigateOptions()
        		.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        		.setTimeout(120000));//hard coded here since sometime application goes very slow, but it' dynamic        		
    }

    public void waitForLoadState() {
        getPage().waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(pageTimeout));
    }

    public void waitForUrl(String urlPattern) {
        getPage().waitForURL(urlPattern, new Page.WaitForURLOptions().setTimeout(pageTimeout));
    }
    
    /*
	 * This method accepts Locator as argument and wait till the element visible,
	 * wait is max out till ElementTimeout of 5sec
	 */
	public Locator waitVisible(Locator locator) {
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
				.setTimeout(elementTimeout));
		return locator;
	}
	
    public String getCurrentUrl() {
        return getPage().url();
    }

    public String getTitle() {
        return getPage().title();
    }
    
    public void navigateBack() {
    	getPage().goBack();
    }
 // ============= KENDO + ANGULAR HELPERS ==================

    /**
     * Wait for Kendo loading mask to disappear
     */
    public void waitForKendoLoadingComplete() {
        try {
            getPage().waitForSelector(".k-loading-mask",
                    new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(elementTimeout));
        } catch (TimeoutError e) {
            // Loading mask might not appear, continue
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for Kendo loading", e);
        }
    }

    /**
     * Wait for all Kendo loaders to disappear.
     * Comprehensive wait - checks multiple loader types.
     */
    public void waitForAllKendoLoadersComplete() {
        String loaderSelector = ".k-loading-mask, .k-i-loading, .k-busy, .k-loading-image";
        try {
            // Wait for any loader to appear (with short timeout)
            try {
                getPage().locator(loaderSelector).first().waitFor(
                    new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(2000));
            } catch (TimeoutError e) {
                // No loader appeared - that's fine
            }

            // Now wait for all loaders to be hidden
            getPage().waitForSelector(loaderSelector,
                    new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(elementTimeout));

        } catch (TimeoutError e) {
            // Loaders might not appear, continue
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for all Kendo loaders", e);
        }
    }

    /**
     * Wait for Kendo grid to fully load.
     * Waits for loading mask + grid content to appear.
     */
    public void waitForKendoGridLoaded(String gridSelector) {
        try {
            waitForAllKendoLoadersComplete();

            getPage().locator(gridSelector).waitFor(
                    new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(elementTimeout));

            getPage().waitForSelector(
                    gridSelector + " tbody tr, " + gridSelector + " .k-grid-norecords",
                    new Page.WaitForSelectorOptions().setTimeout(elementTimeout));

        } catch (TimeoutError e) {
            throw new RuntimeException("Kendo grid did not load: " + gridSelector, e);
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for Kendo grid", e);
        }
    }

    /**
     * Wait for Kendo dropdown/combobox to be ready.
     */
    public void waitForKendoDropdownReady(String dropdownSelector) {
        try {
            waitForAllKendoLoadersComplete();

            Locator dropdown = getPage().locator(dropdownSelector);
            dropdown.waitFor(
                    new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(elementTimeout));

            // Wait for dropdown to be enabled
            getPage().waitForCondition(() -> !dropdown.isDisabled(),
                    new Page.WaitForConditionOptions().setTimeout(elementTimeout));

        } catch (TimeoutError e) {
            throw new RuntimeException("Kendo dropdown not ready: " + dropdownSelector, e);
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for Kendo dropdown", e);
        }
    }

    /**
     * Wait for Angular to stabilize.
     * Use after navigation or major state changes.
     */
    public void waitForAngularStable() {
        try {
            getPage().evaluate("() => new Promise(resolve => {" +
                    "  if (window.getAllAngularTestabilities) {" +
                    "    const testabilities = window.getAllAngularTestabilities();" +
                    "    const count = testabilities.length;" +
                    "    let doneCount = 0;" +
                    "    testabilities.forEach(t => {" +
                    "      t.whenStable(() => {" +
                    "        doneCount++;" +
                    "        if (doneCount === count) resolve();" +
                    "      });" +
                    "    });" +
                    "  } else {" +
                    "    resolve();" +
                    "  }" +
                    "})");
        } catch (Exception e) {
            // Angular might not be available
        }
    }

    /**
     * Comprehensive wait for Kendo Angular page.
     * Use after page navigation or major operations.
     * Combines Angular stability + Kendo loaders.
     */
    public void waitForKendoAngularPageReady() {
        try {
            waitForAngularStable();
            waitForAllKendoLoadersComplete();
            getPage().waitForTimeout(200);
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for Kendo Angular page", e);
        }
    }

 // ============= MULTI-TENANT HELPERS ==================

    /**
     * Select tenant in multi-tenant popup.
     */
    public void selectTenant(String tenantPopupSelector, String tenantName) {
        try {
            System.out.println("  Selecting tenant: " + tenantName);
            
            Locator chooseAccountPopup = locator(tenantPopupSelector);
            
            Locator tenantSpan = chooseAccountPopup.locator("span",
                    new Locator.LocatorOptions().setHasText(tenantName));
            
            if (tenantSpan.count() == 0) {
                throw new RuntimeException("Tenant not found in popup: " + tenantName);
            }
            
            Locator tenantRow = tenantSpan.locator("xpath=ancestor::div[contains(@class,'divWrap')]");
            Locator chooseBtn = tenantRow.locator("button", 
                    new Locator.LocatorOptions().setHasText("Choose"));
            
            clickOnElement(chooseBtn.first());
            
            System.out.println("  ✓ Tenant selected: " + tenantName);
            
        } catch (Exception e) {
            throw new RuntimeException("Error selecting tenant: " + tenantName, e);
        }
    }

    /**
     * Handle tenant selection for a specific role.
     * CRITICAL: Called after login/session reuse.
     */
    public void handleTenantSelectionForRole(String tenantPopupSelector, String role) {
        String tenantName = PropertiesLoader.getTenantForRole(role);
        
        if (tenantName == null) {
            System.out.println("  Single-tenant user (no tenant selection needed)");
            return;
        }
        
        System.out.println("  Multi-tenant user, tenant: " + tenantName);
        
        try {
            getPage().waitForTimeout(1500);
            
            Locator popup = locator(tenantPopupSelector);
            
            if (isVisible(popup)) {
                System.out.println("  Tenant popup detected");
                selectTenant(tenantPopupSelector, tenantName);
                waitForKendoAngularPageReady();
            } else {
                System.out.println("  No tenant popup appeared");
            }
        } catch (Exception e) {
            System.out.println("  No tenant popup appeared");
        }
    }

    // ============= UTILITY HELPERS ==================

    /**
     * Returns current timestamp in custom format.
     */
    public String getCurrentTimestamp(String pattern) {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
    //================
    
    
    /**
     * Handle tenant selection if multi-tenant popup appears.
     * CRITICAL: This must be called after session reuse.
     * 
     * @param tenantPopupSelector Selector for the tenant popup
     * @param tenantName Name of tenant to select
     */
    /*======== IMP this method is commented, since it only needs during existing session invoke
    public void handleTenantSelectionIfVisible(String tenantPopupSelector, String tenantName) {
        try {
            // Give popup time to appear
            getPage().waitForTimeout(1500);
            
            Locator popup = locator(tenantPopupSelector);
            
            if (isVisible(popup)) {
                System.out.println("  Tenant popup detected");
                selectTenant(tenantPopupSelector, tenantName);
                
                // Wait for popup to close
                waitForKendoAngularPageReady();
            }
        } catch (Exception e) {
            // Popup might not appear for single-tenant users - that's OK
            System.out.println("  No tenant popup (single-tenant user)");
        }
    }
    */
}