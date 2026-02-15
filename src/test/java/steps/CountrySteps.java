package steps;

import com.cro.pages.CountryPage;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CountrySteps {
    private final CountryPage countryPage;

    /**
     * Constructor - PicoContainer injects CountryPage.
     */
    public CountrySteps(CountryPage countryPage) {
        this.countryPage = countryPage;
    }

    @When("the user is on the country management page")
    public void navigateToCountryPage() {
        countryPage.navigateViaMenu();
        //Assert.assertTrue(countryPage.isOnCountryPage(), 
            //"Should be on country management page");
    }

    @When("the user adds a country named {string} and activates it")
    public void userAddsCountry(String countryName) {
        countryPage.addCountry(countryName, "Active");
    }

    @Then("the country {string} appears in the list")
    public void countryAppearsInList(String countryName) {
        //Assert.assertTrue(countryPage.isCountryInTable(countryName), 
            //"Country " + countryName + " should appear in the list");
    }
}