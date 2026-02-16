package steps;

import org.testng.Assert;

import com.cro.config.PropertiesLoader;
import com.cro.pages.CountryPage;
import com.cro.pages.HomePage;
import com.cro.playwright.BrowserManager;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CountrySteps {

	private final CountryPage countryPage;
    private final HomePage homePage;
    private String generatedCountryName;

    public CountrySteps(CountryPage countryPage, HomePage homePage) {
        this.countryPage = countryPage;
        this.homePage = homePage;
    }

    @When("the user is on the country management page")
    public void navigateToCountryPage() {
        // CRITICAL: Navigate + handle tenant FIRST (only on first When step)
        String currentUrl = BrowserManager.getPage().url();
        
        if (currentUrl.equals("about:blank")) {
            System.out.println("  Navigating to application...");
            String baseUrl = PropertiesLoader.getBaseUrl();
            BrowserManager.getPage().navigate(baseUrl);
            
            // Wait for page load
            BrowserManager.getPage().waitForLoadState(
                com.microsoft.playwright.options.LoadState.LOAD);
            
            try {
                BrowserManager.getPage().waitForTimeout(10000);
            } catch (Exception e) {
                // Ignore
            }
            
            // Handle tenant selection
            homePage.handleTenantSelection();
        }
        
        // Navigate to country management
        countryPage.navigateViaMenu();
        /*
        Assert.assertTrue(countryPage.isOnCountryPage(), 
            "Should be on country management page");
            */
    }
    /*

    @And("the user adds a country named {string} and activates it")
    public void userAddsCountry(String countryName) {
        generatedCountryName = countryPage.addCountry(countryName, "Active");
        System.out.println("  Generated country name: " + generatedCountryName);
    }

    @Then("the country {string} appears in the list")
    public void countryAppearsInList(String countryName) {
        Assert.assertTrue(countryPage.isCountryInTable(countryName), 
            "Country " + countryName + " should appear in the list");
        System.out.println("  ✅ Country verified: " + generatedCountryName);
    }
    */
}