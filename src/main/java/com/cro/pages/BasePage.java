package com.cro.pages;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all page objects.
 */
public class BasePage {

    protected final int elementTimeout;
    protected final int pageTimeout;

    public BasePage() {
        this.elementTimeout = PropertiesLoader.getElementTimeout();
        this.pageTimeout = PropertiesLoader.getPageTimeout();
    }

    protected Page getPage() {
        return BrowserManager.getPage();
    }

    // ========== LOCATOR CREATION ==========

    protected Locator locator(String selector) {
        return getPage().locator(selector);
    }

    protected Locator getByRole(AriaRole role, String name) {
        return getPage().getByRole(role, new Page.GetByRoleOptions().setName(name));
    }

    protected Locator getByRoleWithinParent(Locator parent, AriaRole role, String childName) {
        return parent.getByRole(role, new Locator.GetByRoleOptions().setName(childName));
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
        getPage().navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(120000));
    }

    public void waitForLoadState() {
        getPage().waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(pageTimeout));
    }

    public void waitForUrl(String urlPattern) {
        getPage().waitForURL(urlPattern, new Page.WaitForURLOptions().setTimeout(pageTimeout));
    }

    public String getCurrentUrl() {
        return getPage().url();
    }

    public String getTitle() {
        return getPage().title();
    }

    // ============= KENDO + ANGULAR HELPERS ==================

    public void waitForKendoLoadingComplete() {
        try {
            getPage().waitForSelector(".k-loading-mask",
                    new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(elementTimeout));
        } catch (TimeoutError e) {
            // Loading mask might not appear
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for Kendo loading", e);
        }
    }

    public void waitForAllKendoLoadersComplete() {
        String loaderSelector = ".k-loading-mask, .k-i-loading, .k-busy, .k-loading-image";
        try {
            try {
                getPage().locator(loaderSelector).first().waitFor(
                    new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(2000));
            } catch (TimeoutError e) {
                // No loader appeared
            }

            getPage().waitForSelector(loaderSelector,
                    new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(elementTimeout));

        } catch (TimeoutError e) {
            // Loaders might not appear
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for all Kendo loaders", e);
        }
    }

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

    public String getCurrentTimestamp(String pattern) {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
}