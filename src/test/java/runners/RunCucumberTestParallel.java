package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "classpath:features",
        glue = { "steps", "com.cro.hooks" },
        tags = "@smoke and @high",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        }
)
public class RunCucumberTestParallel extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = true)  // ← TRUE for parallel
    public Object[][] scenarios() {
        return super.scenarios();
    }
}