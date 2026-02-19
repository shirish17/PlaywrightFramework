package com.cro.pages;

import org.testng.Assert;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Country Management page object.
 * EXTENDS BasePage for direct access to actions.
 */
public class CountryPage extends BasePage {
	// ========== Multi Tenant Selectors ==========
		private static final String TENANT_POPUP = "#showMultiTenantPopup";
    // ========== System Configuration Selectors ==========
	private static final String CONFIGURATION_LINK = "#systemTab";
	private static final String SYSTEM_CONFIG_LINK="ul.dropdown_menu a[routerlink='/console/systemConfig']";	
	private static final String LINK_ROUTER = "a[routerlink='/console/systemConfig']";	
	private static final String LINK_NAME = "System Configuration";
	private static final String SYSTEM_CONFIG_URL = "**/console/systemConfig";	
	// ========== LIST(S) TAB SELECTORS ==========
	private static final String LISTS_TAB = "List(s)";	
	private static final String COUNTRY_NAME_LABEL = "Country Name";
	private static final String NEW_OPTION_LINK="New Option";
	private static final String NEW_OPTION_DIALOG = "div[role='dialog']:has(h2:has-text('New Option'))";
	private static final String OPTION_NAME_INPUT = "div:has(> span:has-text('Option Name')) input.k-input-inner[type='text']";
	private static final String SAVE_BUTTON_IN_DIALOG = "Save";
	private static final String SAVE_BUTTON_IN_ACTIONS_MENU = "span.leftSideBarActionItemsLabel:has-text('Save')";
	private static final String AVAILABLE_OPTION_CONTAINER =
	        ":has(h2:has-text('Available Options'), " +
	        " h3:has-text('Available Options'), " +
	        " label:has-text('Available Options'), " +
	        " span:has-text('Available Options')) " +
	        " ul[role='listbox'].k-list-ul";
	private static final String AVAILABLE_OPTION_CONTAINER_BY_ROLE="ul[role='listbox'].k-list-ul"; //this is for fallback plan
    private static final String DELETE_OPTION_LINK_IN_ACTION=" Delete Option";
    private static final String DELETE_OPTION_DIALOG="Delete Option"; //this seems duplicate but purposely kept to avoid confusion
    private static final String DELETE_BUTTON_IN_DIALOG = "Delete";
    private static final String DELETE_SUCCESS_MESSAGE="Success! Option Deleted successfully";
	// ========== LOCATORS ==========
    //Configuration link on top menu
    private Locator configurationMenuLink() {
        return locator(CONFIGURATION_LINK).first();
    } 
    
  //System configuration link under configuration
    private Locator systemConfigLink() {
    	return locator(SYSTEM_CONFIG_LINK);
    } 
    
    //List(s) tab
    private Locator listsTab() {
        return getByRole(AriaRole.TAB, LISTS_TAB);
    }
    
    private Locator countryNameLabel() {
        return getLocatorByExactTextMatch(COUNTRY_NAME_LABEL, true);
    }
    
    //New Options under actions
    private Locator newOptionMenu() {
        return getLocatorByExactTextMatch(NEW_OPTION_LINK, true);
    }
    
    //New optoin Dialog
    private Locator newOptionDialog() {
        return locator(NEW_OPTION_DIALOG);
    }
   
    //Option Name input on Dialog
    private Locator optionNameInput() {
        return newOptionDialog().locator(OPTION_NAME_INPUT);
    }
   
    private Locator saveButtonInDialog() {
    	return getByRoleWithinParent(newOptionDialog(),AriaRole.BUTTON,SAVE_BUTTON_IN_DIALOG);        
    }
    
    private Locator saveButtonInActionsMenu() {
        return locator(SAVE_BUTTON_IN_ACTIONS_MENU);
    }
    //Success message after saving from actions menu
    private Locator successMessage() {
        return getByTagAndText("div", "Success", 3);
    }
    
    //Available Actions list section
    private Locator availableOptionListBox() {
    	return locator(AVAILABLE_OPTION_CONTAINER).first();
    }
    
 // Available options list box container fallback strategy
    private Locator availableOptionListBox_fallback() {
    	return locator(AVAILABLE_OPTION_CONTAINER_BY_ROLE).first();
    }
	
  //Delete Option under action menu
    private Locator deleteOptionMenu() {
        return getByRole(AriaRole.BUTTON,DELETE_OPTION_LINK_IN_ACTION);
    }
    
  //Delete optoin Dialog
    private Locator deleteOptionDialog() {
        return getByRoleByExact(AriaRole.HEADING,DELETE_OPTION_DIALOG);
    }
    
    //Delete button in Dialog 
    private Locator deleteButtonInInDialog() {
        return getByRoleByExact(AriaRole.BUTTON,DELETE_BUTTON_IN_DIALOG);
    }
    
    //Delete success message DELETE_SUCCESS_MESSAGE
    private Locator successMessageOnDelete() {
        return getByAlert(DELETE_SUCCESS_MESSAGE);
    }
    
    // ========== ACTIONS ==========
    /**
     * Navigate to System Configuration page via menu.
     * This is the method called by CountrySteps.navigateViaMenu()
     * @throws InterruptedException 
     */
    public CountryPage navigateViaMenu() throws InterruptedException {
        System.out.println("  Navigating to Country Management via Configuration menu...");
        waitForKendoAngularPageReady();        
        // Hover System tab
        configurationMenuLink().scrollIntoViewIfNeeded();
        configurationMenuLink().hover();
                
        //Click on System configuration link under menu
        Thread.sleep(2000);
        clickOnElement(systemConfigLink());        
        
        // Wait for SPA navigation
        waitForUrl(SYSTEM_CONFIG_URL);
        waitForKendoAngularPageReady();
        
        //List(s) tab
        waitForVisible(listsTab());
        Thread.sleep(5000);
        clickOnElement(listsTab());
        waitForKendoAngularPageReady();
                
        System.out.println("On Lists tab for Country Management");
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
  
    public String addCountry(String countryName, String status) {
        System.out.println("  Adding new country: " + countryName);
        
        //Click County Name Label        
        clickOnElement(countryNameLabel());
        waitForKendoAngularPageReady();
        
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
        
        //Input text in Option name field
        input.fill(fullCountryName);
        
        // Click Save in dialog
        clickOnElement(saveButtonInDialog());
        waitForKendoAngularPageReady();
        
        System.out.println("Country added: " + fullCountryName);
        
        // Save the list configuration
        clickOnElement(saveButtonInActionsMenu());
        waitForKendoLoadingComplete();
        
        // Wait for success message
        successMessage().waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(elementTimeout));
        
        String messageAfterSaveFromActionMenu = getTextContent(successMessage()).trim();
        System.out.println(messageAfterSaveFromActionMenu);
        navigatePreviousPage();
        String expectedMessage="Success";
        Assert.assertTrue(messageAfterSaveFromActionMenu.contains(expectedMessage),"Expected:it should contain word: "+expectedMessage+".Actual: "+messageAfterSaveFromActionMenu);
        
        return fullCountryName;
    }
	
    /**
     * Check if on Country Name visible on System Configuration-> List(s) tab.
     */
    public boolean isCountryNameLabelVisible() {
    	waitForVisible(countryNameLabel());
        return countryNameLabel().isVisible();
    }   

    /**
     * Check if country exists in table.
     * For now, we verify via success message (country saved successfully).
     */
  
    public boolean isCountryDisplayedInAvailableOptions(String countryName) {
        try {
        	boolean elementVisible=false;
        	//Click County Name Label, since after save the selection is not retained       
            clickOnElement(countryNameLabel());
            waitForKendoAngularPageReady();
            
        	/*Country name shows under "Available Options" ListBox, so first step is to  get the list container,
			list container is defined at page level, if it's not available below fallback mechanism
			*/	
        	 Locator listContainer = resolveAvailableOptionsListBox();
        	// Wait for container to be visible
        	 waitForVisible(listContainer);
        	 
        	// 2) Build a locator for an <li role='option'> that contains a <label> with our country text
        	 Locator option=getByRoleWithinParentExact(listContainer,AriaRole.OPTION,countryName);
        	 Locator countryNameFound = scrollFindWithinParent(
        	            listContainer,
        	            option,
        	            /* maxAttempts   */ 25,
        	            /* debounceMs    */ 100,
        	            /* initialWaitMs */ 500   // small extra guard (optional)
        	        );//* maxAttempts   * --> this is comment to know the parameter name
        	    if (countryNameFound == null) {
        	    	// Not found within limit
                    return false;
        	    }
        	    countryNameFound.first().scrollIntoViewIfNeeded();
        	    elementVisible=countryNameFound.first().isVisible();
        	    navigatePreviousPage();
        	    waitForKendoAngularPageReady();
        	    return elementVisible; 	
			
        } catch (Exception e) {
        	 // Playwright exceptions → treat as not found for this boolean API
            return false;
        }
		
    }
	

	private Locator resolveAvailableOptionsListBox() {
		Locator primary = availableOptionListBox();   // PRIMARY selector
	    if (primary.count() > 0) {
	        return primary;                           // primary exists → use it
	    }
	    Locator fallback = availableOptionListBox_fallback(); // FALLBACK selector
	    return fallback;                                        // primary missing → use fallback
	}

	public String deleteCountryByNameFromSystemConfiguration(String countryName) {
		String deletionResultMessage ="";
		//Click County Name Label        
        clickOnElement(countryNameLabel());
        waitForKendoAngularPageReady();
        
      //Click County Name Label, since after save the selection is not retained       
        clickOnElement(countryNameLabel());
        waitForKendoAngularPageReady();
        
    	/*Country name shows under "Available Options" ListBox, so first step is to  get the list container,
		list container is defined at page level, if it's not available below fallback mechanism
		*/	
    	 Locator listContainer = resolveAvailableOptionsListBox();
    	// Wait for container to be visible
    	 waitForVisible(listContainer);
    	 
    	// 2) Build a locator for an <li role='option'> that contains a <label> with the country text
    	 Locator option=getByRoleWithinParentExact(listContainer,AriaRole.OPTION,countryName.trim());
    	 Locator countryNameFound = scrollFindWithinParent(
    	            listContainer,
    	            option,
    	            /* maxAttempts   */ 25,
    	            /* debounceMs    */ 100,
    	            /* initialWaitMs */ 500   // small extra guard (optional)
    	        );//* maxAttempts   * --> this is comment to know the parameter name
    	    if (countryNameFound == null) {
    	    	// Not found within limit
                return deletionResultMessage; // this stage string will be returned as null
    	    }
    	    countryNameFound.first().scrollIntoViewIfNeeded();    	       	    
    	    // selects the country in the listbox, which needs to be deleted
    	    clickOnElement(countryNameFound.first());
    	    waitForKendoAngularPageReady();
    	    
    	    //Click delete link under actions 
    	    clickOnElement(deleteOptionMenu());
    	    
    	    //waiting for Delete dialog
    	    // Wait for dialog
    	    deleteOptionDialog().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(elementTimeout));            
            waitForKendoLoadingComplete();
            
            deleteButtonInInDialog().scrollIntoViewIfNeeded();
            clickOnElement(deleteButtonInInDialog());
         
            waitForVisible(successMessageOnDelete());
            
            deletionResultMessage= successMessageOnDelete().innerText().trim();
            
            System.out.println("Flash delete success message: "+deletionResultMessage);
            
            //Assert.assertTrue(successMessageOnDelete().innerText().trim().contains("Deleted successfully"));
            
            navigatePreviousPage();//optional step otherwise record will be locked (application behavior) 
            return deletionResultMessage;
		
	}

	public String getCountryByNameFromSystemConfiguration(String countryName) {
		String countryNameFromAvailableList="";
		try {
			clickOnElement(countryNameLabel());
	        waitForKendoAngularPageReady();
	        
	    	/*Country name shows under "Available Options" ListBox, so first step is to  get the list container,
			list container is defined at page level, if it's not available below fallback mechanism
			*/	
	    	 Locator listContainer = resolveAvailableOptionsListBox();
	    	// Wait for container to be visible
	    	 waitForVisible(listContainer);
	    	// 2) Build a locator for an <li role='option'> that contains a <label> with our country text
	    	 Locator option=getByRoleWithinParentExact(listContainer,AriaRole.OPTION,countryName);
	    	 Locator countryNameFound = scrollFindWithinParent(
	    	            listContainer,
	    	            option,
	    	            /* maxAttempts   */ 25,
	    	            /* debounceMs    */ 100,
	    	            /* initialWaitMs */ 500   // small extra guard (optional)
	    	        );//* maxAttempts   * --> this is comment to know the parameter name
	    	    if (countryNameFound == null) {
	    	    	// Not found within limit
	                return countryNameFromAvailableList;
	    	    }
	    	    countryNameFound.first().scrollIntoViewIfNeeded();	    	    
	    	    countryNameFromAvailableList=countryNameFound.first().innerText().trim();
	    	    countryNameFromAvailableList=normalizeUiText(countryNameFromAvailableList); //this will normalize UI text and return in same variable
	    	    
	    	    navigatePreviousPage();//optional step otherwise record will be locked (application behavior)
	    	 
		}catch (Exception e) {
			e.printStackTrace();
		}
		return countryNameFromAvailableList;
		
		
	}
   
}