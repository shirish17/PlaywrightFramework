package steps;

import com.cro.config.PropertiesLoader;
import com.cro.context.RoleContext;
import com.cro.pages.LoginPage;
import com.cro.playwright.SessionManager;
import io.cucumber.java.en.Given;

/**
 * Login step definitions.
 */
public class LoginSteps {

    @Given("I login as role {string}")
    public void i_login_as_role(String role) {
        System.out.println("  🔐 Background: Setting role = " + role);

        RoleContext.setRole(role);

        String username = PropertiesLoader.getUsername(role);
        String password = PropertiesLoader.getPassword(role);
        String baseUrl = PropertiesLoader.getBaseUrl();

        // Create or reuse session (context reused per role)
        SessionManager.createOrReuseSession(role, (page) -> {
            LoginPage.performLoginForSession(page, username, password, baseUrl);
        });

        System.out.println("  ✓ Session ready for role: " + role);
    }
}