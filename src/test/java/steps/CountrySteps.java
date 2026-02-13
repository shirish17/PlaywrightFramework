package steps;

import io.cucumber.java.en.When;

public class CountrySteps {

    private TestContext testContext;

    public CountrySteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @When("the user is on the country management page")
    public void navigateToCountryPage() {
        testContext.getActions().click("#menu-country");
    }
}