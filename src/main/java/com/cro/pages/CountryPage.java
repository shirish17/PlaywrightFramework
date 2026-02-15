package com.cro.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Country Management page object.
 * EXTENDS BasePage for direct access to actions.
 */
public class CountryPage extends BasePage {

    // ========== ELEMENT DEFINITIONS ==========
    private static final String LISTS_TAB = "List(s)";
    private static final String MENU_COUNTRY = "#menu-country";
    private static final String ADD_COUNTRY_BUTTON = "#addCountryBtn";
    private static final String COUNTRY_NAME_INPUT = "#countryName";
    private static final String STATUS_DROPDOWN = "#status";
    private static final String SAVE_BUTTON = "#saveBtn";
    private static final String COUNTRY_TABLE = ".country-table";

    // ========== LOCATORS ==========

    private Locator menuCountry() {
        return locator(MENU_COUNTRY);
    }

    private Locator listsTab() {
        return getByRole(AriaRole.TAB, LISTS_TAB);
    }

    private Locator addCountryButton() {
        return locator(ADD_COUNTRY_BUTTON);
    }

    private Locator countryNameInput() {
        return locator(COUNTRY_NAME_INPUT);
    }

    private Locator statusDropdown() {
        return locator(STATUS_DROPDOWN);
    }

    private Locator saveButton() {
        return locator(SAVE_BUTTON);
    }

    private Locator countryTable() {
        return locator(COUNTRY_TABLE);
    }

    // ========== ACTIONS ==========

    /**
     * Navigate to country page via menu.
     */
    public CountryPage navigateViaMenu() {
        clickOnElement(menuCountry());
        waitForLoadState();
        return this;
    }

    /**
     * Click on Lists tab.
     */
    public CountryPage clickListsTab() {
        clickOnElement(listsTab());
        return this;
    }

    /**
     * Add a new country.
     */
    public CountryPage addCountry(String countryName, String status) {
        clickOnElement(addCountryButton());
        fillElement(countryNameInput(), countryName);
        selectOption(statusDropdown(), status);
        clickOnElement(saveButton());
        waitForLoadState();
        return this;
    }

    /**
     * Check if country exists in table.
     */
    public boolean isCountryInTable(String countryName) {
        String tableText = getTextContent(countryTable());
        return tableText != null && tableText.contains(countryName);
    }

    /**
     * Verify on country page.
     */
    public boolean isOnCountryPage() {
        return getCurrentUrl().contains("country");
    }
}
