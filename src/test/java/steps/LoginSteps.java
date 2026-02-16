package steps;
import com.cro.config.PropertiesLoader;
import com.cro.context.RoleContext;
import com.cro.pages.HomePage;
import com.cro.pages.LoginPage;
import com.cro.playwright.BrowserManager;
import com.cro.playwright.SessionManager;

import io.cucumber.java.en.Given;
/**
 * Login step definitions.
 * 
 * CRITICAL FIX: After session reuse, we must navigate to baseUrl to activate the session.
 * Without this, the page is blank and tenant selection fails.
 */
public class LoginSteps {

    @Given("I login as role {string}")
    public void i_login_as_role(String role) {
        System.out.println("  🔐 Background: Setting role = " + role);

        RoleContext.setRole(role);

        String username = PropertiesLoader.getUsername(role);
        String password = PropertiesLoader.getPassword(role);
        String baseUrl = PropertiesLoader.getBaseUrl();

        // Create or reuse session
        SessionManager.createOrReuseSession(role, (page) -> {
            LoginPage.performLoginForSession(page, username, password, baseUrl);
        });

        System.out.println("  ✓ Session created/reused");
    }
}