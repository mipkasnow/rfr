package io.student.rangiffler.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public final class Elements {
    private final Page page;

    public Elements(Page page) {
        this.page = page;
    }

    public Locator byRole(AriaRole role) {
        return page.getByRole(role);
    }

    public Locator byRole(AriaRole role, String name) {
        return page.getByRole(role, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    public Locator byRole(Locator root, AriaRole role, String name) {
        return root.getByRole(role, new Locator.GetByRoleOptions().setName(name).setExact(true));
    }

    public Locator byText(String text) {
        return page.getByText(text, new Page.GetByTextOptions().setExact(true));
    }

    public Locator byText(Locator root, String text) {
        return root.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }

    public Locator byTestId(String testId) {
        return page.getByTestId(testId);
    }
}
