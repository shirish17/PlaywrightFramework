package steps;

import com.microsoft.playwright.Page;
import com.cro.playwright.PageActions;

public class TestContext {

    private Page page;
    private PageActions actions;

    public void setPage(Page page) {
        this.page = page;
        this.actions = new PageActions(page);
    }

    public Page getPage() {
        return page;
    }

    public PageActions getActions() {
        return actions;
    }
}
