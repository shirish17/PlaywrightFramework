package steps;
import com.cro.context.RoleContext;
import io.cucumber.java.en.Given;

public class LoginSteps {

    @Given("I login as role {string}")
    public void i_login_as_role(String role) {
        RoleContext.setRole(role);
    }
}
