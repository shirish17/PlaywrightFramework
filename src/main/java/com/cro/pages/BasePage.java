package com.cro.pages;

import com.cro.config.PropertiesLoader;
import com.cro.playwright.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all page objects.
 */
public class BasePage {

	protected final int elementTimeout;
	protected final int pageTimeout;

	public BasePage() {
		this.elementTimeout = PropertiesLoader.getElementTimeout();
		this.pageTimeout = PropertiesLoader.getPageTimeout();
	}

	protected Page getPage() {
		return BrowserManager.getPage();
	}

	// ========== LOCATOR CREATION ==========

	protected Locator locator(String selector) {
		return getPage().locator(selector);
	}

	/*
	 * * Because we didn’t set .setExact(true), Playwright uses a partial (contains)
	 * on the provided string locator
	 */
	protected Locator getByRole(AriaRole role, String name) {
		return getPage().getByRole(role, new Page.GetByRoleOptions().setName(name));
	}

	/*
	 * This will look for the exact match of the provided string
	 */
	protected Locator getByRoleByExact(AriaRole role, String name) {
		return getPage().getByRole(role, new Page.GetByRoleOptions().setName(name).setExact(true));
	}

	// This strategy will look for child under parent with exact name property of
	// child, setExact(true)
	protected Locator getByRoleWithinParentExact(Locator parent, AriaRole role, String childName) {
		return parent.getByRole(role, new Locator.GetByRoleOptions().setName(childName).setExact(true));
	}

	/*
	 * Because we didn’t set .setExact(true), Playwright uses a partial (contains)
	 * match on the element’s accessible name. That means if childName =
	 * "United States", this can match: * "United States"
	 * "United States Minor Outlying Islands" "The United States" "United  States"
	 * (extra spaces) Even "united states" (case differences) * Playwright
	 * normalizes whitespace and is case‑insensitive for these name matches. So your
	 * current helper effectively behaves like contains (case‑insensitive,
	 * space‑normalized).
	 */
	protected Locator getByRoleWithinParent(Locator parent, AriaRole role, String childName) {
		return parent.getByRole(role, new Locator.GetByRoleOptions().setName(childName));
	}

	protected Locator getByText(String text) {
		return getPage().getByText(text);
	}

	protected Locator getByPlaceholder(String placeholder) {
		return getPage().getByPlaceholder(placeholder);
	}

	protected Locator getByLabel(String label) {
		return getPage().getByLabel(label);
	}

	/**
	 * Create locator by text content with exact match option
	 * 
	 * @param text  - Text to search for
	 * @param exact - If true, requires exact match
	 * @return Locator
	 */
	public Locator getLocatorByExactTextMatch(String text, boolean exact) {
		return getPage().getByText(text, new Page.GetByTextOptions().setExact(exact));
	}

	/**
	 * Create locator by tag + partial text match with optional index
	 *
	 * @param tag   - HTML tag (e.g. div, span)
	 * @param text  - Text to match
	 * @param index - Zero-based index (use -1 if not needed and .nth() is NOT
	 *              called)
	 */
	public Locator getByTagAndText(String tag, String text, int index) {
		Locator locator = getPage().locator(tag).filter(new Locator.FilterOptions().setHasText(text));

		return index >= 0 ? locator.nth(index) : locator;
	}

	/*
	 * /** Return a Locator for a success/alert toast that contains the given text
	 * fragment. This does NOT wait; it only builds a resilient locator with
	 * fallbacks.
	 *
	 * Order: 1) role=alert INSIDE Kendo region, with accessible-name containing the
	 * fragment 2) text match INSIDE Kendo region 3) global text match as a last
	 * resort
	 */

	public Locator getByAlert(String textFragment) {
		final String KENDO_REGION_SELECTOR = ".k-notification, .k-toast, .k-message, kendo-notification-container";

		try {
			// 1) Scope to Kendo region
			Locator region = getPage().locator(KENDO_REGION_SELECTOR).first();

			// ✅ Role-based with scope INSIDE the region, otherwise it will search entire
			// page (wrong)
			Locator roleAlertInRegion = region.getByRole(AriaRole.ALERT,
					new Locator.GetByRoleOptions().setName(textFragment) // contains match by default
			);
			if (roleAlertInRegion.count() > 0) {
				return roleAlertInRegion;
			}

			// 2) Fallback: any element in the region with the text
			Locator textInRegion = region.locator(":scope", new Locator.LocatorOptions().setHasText(textFragment))
					.first();
			if (textInRegion.count() > 0) {
				return textInRegion;
			}

			// 3) Last resort: global text
			return getPage().getByText(textFragment).first();

		} catch (RuntimeException ex) {
			// Defensive fallback so the caller always gets a usable Locator
			return getPage().getByText(textFragment).first();
		}
	}
	// ========== ACTIONS WITH TIMEOUT ==========

	public void clickOnElement(Locator locator) {
		locator.click(new Locator.ClickOptions().setTimeout(elementTimeout));
	}

	public void fillElement(Locator locator, String text) {
		locator.fill(text, new Locator.FillOptions().setTimeout(elementTimeout));
	}

	public void selectOption(Locator locator, String value) {
		locator.selectOption(value, new Locator.SelectOptionOptions().setTimeout(elementTimeout));
	}

	public String getTextContent(Locator locator) {
		return locator.textContent(new Locator.TextContentOptions().setTimeout(elementTimeout));
	}

	public boolean isVisible(Locator locator) {
		return locator.isVisible(new Locator.IsVisibleOptions().setTimeout(elementTimeout));
	}

	public void waitForVisible(Locator locator) {
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(elementTimeout));
	}

	// ========== PAGE NAVIGATION ==========

	public void navigate(String url) {
		getPage().navigate(url,
				new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(120000));
	}

	public void waitForLoadState() {
		getPage().waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD,
				new Page.WaitForLoadStateOptions().setTimeout(pageTimeout));
	}

	public void waitForUrl(String urlPattern) {
		getPage().waitForURL(urlPattern, new Page.WaitForURLOptions().setTimeout(pageTimeout));
	}

	public String getCurrentUrl() {
		return getPage().url();
	}

	public String getTitle() {
		return getPage().title();
	}

	// =========================================
	// 3) SCROLL helper: find option by exact accessible name in a long/virtualized
	// list
//	    Returns the option Locator when found, or null if not found within attempts.
	// =========================================
	/**
	 * Scrolls a (potentially virtualized) parent container by 1 viewport at a time
	 * until the child locator is present & visible, or attempts are exhausted.
	 *
	 * @param parent        Scrollable container (e.g., the listbox
	 *                      <ul>
	 *                      )
	 * @param child         Child locator to look for (e.g., li[role='option'] with
	 *                      exact country)
	 * @param maxAttempts   Number of scroll steps (1 step ≈ one clientHeight)
	 * @param debounceMs    Small wait after each scroll to allow the DOM to render
	 *                      (80–150ms typical)
	 * @param initialWaitMs Initial wait for the parent to become visible (0 to
	 *                      skip)
	 * @return The child locator (same instance passed in) if it becomes visible;
	 *         otherwise null
	 */
	public static Locator scrollFindWithinParent(Locator parent, Locator child, int maxAttempts, int debounceMs,
			int initialWaitMs) {
		// Ensure parent is visible (once)
		if (initialWaitMs > 0) {
			parent.first().waitFor(
					new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(initialWaitMs));
		}

		// Quick check before scrolling
		if (child.count() > 0 && child.first().isVisible()) {
			return child;
		}

		double previousTop = -1;
		for (int i = 0; i < maxAttempts; i++) {
			// Read current scroll metrics
			double top = ((Number) parent.evaluate("el => el.scrollTop")).doubleValue();
			double total = ((Number) parent.evaluate("el => el.scrollHeight")).doubleValue();
			double client = ((Number) parent.evaluate("el => el.clientHeight")).doubleValue();

			// Early exit: bottom reached or no movement
			if (top >= total - client - 1 || top == previousTop) {
				break;
			}
			previousTop = top;

			// Scroll down by one viewport
			parent.evaluate("el => { el.scrollBy(0, el.clientHeight); }");

			// Debounce to allow rendering
			if (debounceMs > 0) {
				parent.page().waitForTimeout(debounceMs);
			}

			// Re-check child
			if (child.count() > 0 && child.first().isVisible()) {
				return child;
			}
		}

		// Optional: last jump to bottom (may help render last page)
		try {
			parent.evaluate("el => { el.scrollTop = el.scrollHeight; }");
			if (debounceMs > 0)
				parent.page().waitForTimeout(debounceMs);
			if (child.count() > 0 && child.first().isVisible()) {
				return child;
			}
		} catch (RuntimeException ignored) {
		}

		return null;
	}

	/*
	 * This will go back to the previous page, some pages are locked and can't be
	 * used by concurrent users Example: System configuration then that time go to
	 * previous page (usually it connect to home page)
	 */
	public void navigatePreviousPage() {
		getPage().goBack();
	}
	/*
	 * Below method will normalize UI text.the two values “look” the same in
	 * console, but they are not byte‑for‑byte identical. Common culprits: *
	 * Non‑breaking/thin/zero‑width spaces coming from the DOM (e.g., \u00A0,
	 * \u202F, \u200B) Invisible characters like BOM \uFEFF Different whitespace
	 * runs (multi‑space, newlines) Different Unicode normalization (e.g., composed
	 * vs decomposed characters) Example: Country return by Add
	 * event:Auto_CountryName_20260218_134159 Scenario has test data:
	 * Auto_CountryName_20260218_134159
	 */
	public static String normalizeUiText(String textToNormalize) {
	    if (textToNormalize == null) return "";

	    // 1) Normalize Unicode to a compatibility form (handles look‑alike characters, composed forms, etc.)
	    String normalizedText = Normalizer.normalize(textToNormalize, Normalizer.Form.NFKC);

	    // 2) Remove invisible control characters that often sneak in from the DOM
	    normalizedText = normalizedText
	            .replace("\uFEFF", "")  // Byte Order Mark (BOM)
	            .replace("\u200B", "")  // Zero-width space
	            .replace("\u200C", "")  // Zero-width non-joiner
	            .replace("\u200D", "")  // Zero-width joiner
	            .replace("\u2060", ""); // Word joiner

	    // 3) Map non-breaking / thin spaces to regular spaces (so spacing compares cleanly)
	    normalizedText = normalizedText
	            .replace('\u00A0', ' ') // NBSP
	            .replace('\u202F', ' ') // Narrow NBSP
	            .replace('\u2007', ' ');// Figure space

	    // 4) Collapse all runs of whitespace to a single space and trim edges
	    normalizedText = normalizedText.replaceAll("\\s+", " ").trim();

	    return normalizedText;
	}
	
	
	
	
	// ============= KENDO + ANGULAR HELPERS ==================

	public void waitForKendoLoadingComplete() {
		try {
			getPage().waitForSelector(".k-loading-mask",
					new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(elementTimeout));
		} catch (TimeoutError e) {
			// Loading mask might not appear
		} catch (Exception e) {
			throw new RuntimeException("Error waiting for Kendo loading", e);
		}
	}

	public void waitForAllKendoLoadersComplete() {
		String loaderSelector = ".k-loading-mask, .k-i-loading, .k-busy, .k-loading-image";
		try {
			try {
				getPage().locator(loaderSelector).first()
						.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(2000));
			} catch (TimeoutError e) {
				// No loader appeared
			}

			getPage().waitForSelector(loaderSelector,
					new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(elementTimeout));

		} catch (TimeoutError e) {
			// Loaders might not appear
		} catch (Exception e) {
			throw new RuntimeException("Error waiting for all Kendo loaders", e);
		}
	}

	public void waitForAngularStable() {
		try {
			getPage().evaluate("() => new Promise(resolve => {" + "  if (window.getAllAngularTestabilities) {"
					+ "    const testabilities = window.getAllAngularTestabilities();"
					+ "    const count = testabilities.length;" + "    let doneCount = 0;"
					+ "    testabilities.forEach(t => {" + "      t.whenStable(() => {" + "        doneCount++;"
					+ "        if (doneCount === count) resolve();" + "      });" + "    });" + "  } else {"
					+ "    resolve();" + "  }" + "})");
		} catch (Exception e) {
			// Angular might not be available
		}
	}

	public void waitForKendoAngularPageReady() {
		try {
			waitForAngularStable();
			waitForAllKendoLoadersComplete();
			getPage().waitForTimeout(200);
		} catch (Exception e) {
			throw new RuntimeException("Error waiting for Kendo Angular page", e);
		}
	}

	// ============= MULTI-TENANT HELPERS ==================

	public void selectTenant(String tenantPopupSelector, String tenantName) {
		try {
			System.out.println("  Selecting tenant: " + tenantName);

			Locator chooseAccountPopup = locator(tenantPopupSelector);
			Locator tenantSpan = chooseAccountPopup.locator("span",
					new Locator.LocatorOptions().setHasText(tenantName));

			if (tenantSpan.count() == 0) {
				throw new RuntimeException("Tenant not found in popup: " + tenantName);
			}

			Locator tenantRow = tenantSpan.locator("xpath=ancestor::div[contains(@class,'divWrap')]");
			Locator chooseBtn = tenantRow.locator("button", new Locator.LocatorOptions().setHasText("Choose"));

			clickOnElement(chooseBtn.first());

			System.out.println("  ✓ Tenant selected: " + tenantName);

		} catch (Exception e) {
			throw new RuntimeException("Error selecting tenant: " + tenantName, e);
		}
	}

	public void handleTenantSelectionForRole(String tenantPopupSelector, String role) {
		String tenantName = PropertiesLoader.getTenantForRole(role);

		if (tenantName == null) {
			System.out.println("  Single-tenant user (no tenant selection needed)");
			return;
		}

		System.out.println("  Multi-tenant user, tenant: " + tenantName);

		try {
			getPage().waitForTimeout(1500);

			Locator popup = locator(tenantPopupSelector);

			if (isVisible(popup)) {
				System.out.println("  Tenant popup detected");
				selectTenant(tenantPopupSelector, tenantName);
				waitForKendoAngularPageReady();
			} else {
				System.out.println("  No tenant popup appeared");
			}
		} catch (Exception e) {
			System.out.println("  No tenant popup appeared");
		}
	}

	// ============= UTILITY HELPERS ==================

	public String getCurrentTimestamp(String pattern) {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
	}
}