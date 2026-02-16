package com.cro.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Country Management page object.
 * EXTENDS BasePage for direct access to actions.
 */
public class CountryPage extends BasePage {

    // ========== SELECTORS ==========
	private static final String CONFIGURATION_LINK = "#systemTab";
	private static final String SYSTEM_CONFIG_LINK="ul.dropdown_menu a[routerlink='/console/systemConfig']";
	
	private static final String LINK_ROUTER = "a[routerlink='/console/systemConfig']";
	
	private static final String LINK_NAME = "System Configuration";
	private static final String SYSTEM_CONFIG_URL = "**/console/systemConfig";
	private static final String TENANT_POPUP = "#showMultiTenantPopup";
	private static final String LISTS_TAB = "List(s)";
	private static final String COUNTRY_NAME = "Country Name";
	private static final String NEW_OPTIONS = "div[role='dialog']:has(h2:has-text('New Option'))";
	private static final String OPTION_NAME_INPUT = "div:has(> span:has-text('Option Name')) input.k-input-inner[type='text']";
	private static final String SAVEBTN_ON_DIALOG = "Save";
	private static final String SAVEBTN_ON_ACTIONS = "span.leftSideBarActionItemsLabel:has-text('Save')";
	private static final String AVAILABLE_OPTION_CONTAINER =
	        ":has(h2:has-text('Available Options'), " +
	        " h3:has-text('Available Options'), " +
	        " label:has-text('Available Options'), " +
	        " span:has-text('Available Options')) " +
	        " ul[role='listbox'].k-list-ul";
	private static final String AVAILABLE_OPTION_CONTAINER_BY_ROLE="ul[role='listbox'].k-list-ul"; //this is for fallback plan
    
    /*
    private static final String SYSTEM_CONFIG_MENU = "ul.dropdown_menu:has(a[routerlink='/console/systemConfig'])";
    private static final String SYSTEM_CONFIG_LINK = "a[routerlink='/console/systemConfig']";
    private static final String LISTS_TAB = "List(s)";
    private static final String COUNTRY_NAME_LABEL = "text=Country Name";
    private static final String NEW_OPTION_MENU = "text=New Option";
    private static final String NEW_OPTION_DIALOG = "div[role='dialog']:has(h2:has-text('New Option'))";
    private static final String OPTION_NAME_INPUT = "div:has(> span:has-text('Option Name')) input.k-input-inner[type='text']";
    private static final String SAVE_BUTTON_IN_DIALOG = "Save";
    private static final String SAVE_BUTTON_FOOTER = "span.leftSideBarActionItemsLabel:has-text('Save')";
    private static final String SUCCESS_MESSAGE = "div";
    */

    // ========== LOCATORS ==========
    //Configuration link on top menu
    private Locator configurationMenuLink() {
        return locator(CONFIGURATION_LINK).first();
    } 
    
  //System configuration link under configuration
    private Locator systemConfigLink() {
    	return locator(SYSTEM_CONFIG_LINK);
    }
    
    /*
    
    private Locator systemConfigMenu() {
        return locator(SYSTEM_CONFIG_MENU);
    }
    
  
    private Locator systemConfigLink() {
        return systemConfigMenu().locator(SYSTEM_CONFIG_LINK).first();
    }
	*/
    
    /*
    //IMplement this part
    private Locator listsTab() {
        return getByRole(AriaRole.TAB, LISTS_TAB);
    }

    private Locator countryNameLabel() {
        return locator(COUNTRY_NAME_LABEL);
    }

    private Locator newOptionMenu() {
        return locator(NEW_OPTION_MENU);
    }

    private Locator newOptionDialog() {
        return locator(NEW_OPTION_DIALOG);
    }

    private Locator optionNameInput() {
        return newOptionDialog().locator(OPTION_NAME_INPUT);
    }

    private Locator saveButtonInDialog() {
        return newOptionDialog().getByRole(AriaRole.BUTTON, 
            new Locator.GetByRoleOptions().setName(SAVE_BUTTON_IN_DIALOG).setExact(true));
    }

    private Locator saveButtonFooter() {
        return locator(SAVE_BUTTON_FOOTER);
    }

    private Locator successMessage() {
        return locator(SUCCESS_MESSAGE)
            .filter(new Locator.FilterOptions().setHasText("Success"))
            .nth(3);
    }
	*/
    // ========== ACTIONS ==========

    /**
     * Navigate to System Configuration page via menu.
     * This is the method called by CountrySteps.navigateViaMenu()
     */
    public CountryPage navigateViaMenu() {
        System.out.println("  Navigating to System Configuration via menu...");
        
        //waitForKendoAngularPageReady();
        
        // Hover System tab
        configurationMenuLink().scrollIntoViewIfNeeded();
        configurationMenuLink().hover();
        
        waitVisible(systemConfigLink());
        
        //Click on System configuration link under menu
        clickOnElement(systemConfigLink());        
        
        
        // Wait for SPA navigation
        waitForUrl(SYSTEM_CONFIG_URL);
        waitForKendoAngularPageReady();
        
        navigateBack();
        /*
        // Click Lists tab
        listsTab().waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(elementTimeout));
        
        clickOnElement(listsTab());
        waitForKendoAngularPageReady();
        
        // Expand Country Name section
        clickOnElement(countryNameLabel());
        waitForKendoAngularPageReady();
        */
        System.out.println("  ✓ On Country Management section");
        return this;
    }

    /**
     * Add a new country with the given name and status.
     * This is the method called by CountrySteps.addCountry()
     * 
     * @param countryName Base country name (will be timestamped)
     * @param status Status (e.g., "Active")
     * @return The actual country name created (with timestamp)
     */
    /*
    public String addCountry(String countryName, String status) {
        System.out.println("  Adding new country: " + countryName);
        
        // Click New Option
        clickOnElement(newOptionMenu());
        
        // Wait for dialog
        newOptionDialog().waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(elementTimeout));
        
        waitForKendoLoadingComplete();
        
        // Generate timestamped country name
        String timestamp = getCurrentTimestamp("yyyyMMdd_HHmmss");
        String fullCountryName = countryName + "_" + timestamp;
        
        // Fill country name
        Locator input = optionNameInput();
        input.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(elementTimeout));
        
        input.click();
        input.fill(fullCountryName);
        
        // Click Save in dialog
        clickOnElement(saveButtonInDialog());
        waitForKendoAngularPageReady();
        
        System.out.println("  ✓ Country added: " + fullCountryName);
        
        // Save the list configuration
        clickOnElement(saveButtonFooter());
        waitForKendoLoadingComplete();
        
        // Wait for success message
        successMessage().waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(elementTimeout));
        
        String message = getTextContent(successMessage());
        System.out.println("  ✓ " + message);
        
        return fullCountryName;
    }
	
    /**
     * Check if on Country page (System Configuration).
     */
    public boolean isOnCountryPage() {
        return getCurrentUrl().contains("systemConfig");
    }
   

    /**
     * Check if country exists in table.
     * For now, we verify via success message (country saved successfully).
     */
    /*
    public boolean isCountryInTable(String countryName) {
        try {
            return isVisible(successMessage());
        } catch (Exception e) {
            return false;
        }
    }
    */
}