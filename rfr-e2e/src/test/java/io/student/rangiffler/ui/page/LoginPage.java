package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

public final class LoginPage {
  public static final String URL = TestConfig.authUrl() + "/login";
  private final Page page;
  private final CredentialsForm credentials;
  private final Locator signIn;

  public LoginPage(Page page) {
    this.page = page;
    this.credentials = new CredentialsForm(page);
    this.signIn = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public LoginPage fillLoginForm(String username, String password) {
    credentials.fillCredentials(username, password);
    return this;
  }

  @Step("Нажать Sign in")
  public MainPage clickSignIn() {
    signIn.click();
    return new MainPage(page);
  }
}
