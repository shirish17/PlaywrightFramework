package com.cro.playwright;

import com.microsoft.playwright.Page;

public class PageActions {
    private Page page;

    public PageActions(Page page) { this.page = page; }

    public void click(String selector) {
        page.locator(selector).click();
    }

    public void type(String selector, String text) {
        page.locator(selector).fill(text);
    }

    public String getText(String selector) {
        return page.locator(selector).textContent();
    }
}
