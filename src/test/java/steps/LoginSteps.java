package steps;
import com.cro.config.PropertiesLoader;
import com.cro.context.RoleContext;
import com.cro.pages.LoginPage;
import com.cro.playwright.SessionManager;

import io.cucumber.java.en.Given;

public class LoginSteps {

    /**
     * This step is called from Background.
     * It sets the role AND creates/reuses the session immediately.
     */
	 @Given("I login as role {string}")
	    public void i_login_as_role(String role) {
	        System.out.println("  🔐 Background: Setting role = " + role);

	        // Set role in ThreadLocal
	        RoleContext.setRole(role);

	        // Get credentials
	        String username = PropertiesLoader.getUsername(role);
	        String password = PropertiesLoader.getPassword(role);
	        String baseUrl = PropertiesLoader.getBaseUrl();

	        // Create or reuse session, delegating login to LoginPage
	        SessionManager.createOrReuseSession(role, (page) -> {
	            // This callback is only called when creating NEW session
	            LoginPage.performLoginForSession(page, username, password, baseUrl);
	        });
	    }
	}