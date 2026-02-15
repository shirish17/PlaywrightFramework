package com.cro.pages;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;

/**
 * Base class for all page objects.
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
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(elementTimeout));
    }

    // ========== PAGE NAVIGATION ==========

    public void navigate(String url) {
        getPage().navigate(url, new Page
        		.NavigateOptions()
        		.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        		.setTimeout(120000));//hard coded here since sometime application goes very slow
        		//.setTimeout(pageTimeout));
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
}