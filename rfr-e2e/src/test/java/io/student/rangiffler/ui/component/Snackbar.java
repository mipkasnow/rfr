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
        assertThat(alert.getByText(notification.text(), new Locator.GetByTextOptions().setExact(true))).isVisible();
        return this;
    }
}
