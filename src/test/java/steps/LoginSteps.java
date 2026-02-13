package steps;
import com.cro.context.RoleContext;
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
        // Immediately create or reuse session for this role
        SessionManager.createOrReuse(role);
        
    }
}