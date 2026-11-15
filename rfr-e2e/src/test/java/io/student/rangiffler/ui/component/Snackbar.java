package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.domain.Notification;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class Snackbar {
    private final Locator alert;

    public Snackbar(Page page) {
        this.alert = page.getByRole(AriaRole.ALERT);
    }

    @Step("Проверить уведомление: {notification}")
    public Snackbar checkThatShown(Notification notification) {
        return checkThatShown(notification.text());
    }

    @Step("Проверить уведомление с текстом: {text}")
    public Snackbar checkThatShown(String text) {
        assertThat(alert.getByText(text, new Locator.GetByTextOptions().setExact(true))).isVisible();
        return this;
    }

    @Step("Проверить, что уведомление с текстом {text} не показано")
    public Snackbar checkThatNotShown(String text) {
        assertThat(alert.getByText(text, new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        return this;
    }
}
