package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
  public static final String URL = TestConfig.frontUrl() + "/profile";
  private final Page page;
  private final Locator usernameInput;

  public ProfilePage(Page page) {
    this.page = page;
    this.usernameInput = page.getByLabel("Username", new Page.GetByLabelOptions().setExact(true));
  }

  @Step("Открыть профиль")
  public ProfilePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Перезагрузить страницу профиля")
  public ProfilePage reload() {
    page.reload();
    return new ProfilePage(page);
  }

  @Step("Проверить имя пользователя в профиле: {username}")
  public ProfilePage checkThatUsername(String username) {
    assertThat(usernameInput).hasValue(username);
    return this;
  }
}
