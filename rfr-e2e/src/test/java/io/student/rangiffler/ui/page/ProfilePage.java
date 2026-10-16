package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import java.nio.file.Path;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
  public static final String URL = TestConfig.frontUrl() + "/profile";
  private final Page page;
  private final Locator usernameInput;
  private final Locator firstNameInput;
  private final Locator surnameInput;
  private final Locator location;
  private final Locator avatarInput;
  private final Locator avatarImage;
  private final Locator save;
  private final Locator reset;

  public ProfilePage(Page page) {
    this.page = page;
    this.usernameInput = page.getByLabel("Username", new Page.GetByLabelOptions().setExact(true));
    this.firstNameInput = page.getByLabel("First Name", new Page.GetByLabelOptions().setExact(true));
    this.surnameInput = page.getByLabel("Surname", new Page.GetByLabelOptions().setExact(true));
    this.location = page.getByRole(AriaRole.COMBOBOX,
        new Page.GetByRoleOptions().setName("Location").setExact(true));
    this.avatarInput = page.getByLabel("Update avatar", new Page.GetByLabelOptions().setExact(true));
    this.avatarImage = page.getByRole(AriaRole.MAIN).getByRole(AriaRole.IMG,
        new Locator.GetByRoleOptions().setName(Pattern.compile("^$")));
    this.save = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Save").setExact(true));
    this.reset = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Reset").setExact(true));
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

  @Step("Заполнить First Name: {firstName}")
  public ProfilePage fillFirstName(String firstName) {
    firstNameInput.fill(firstName);
    return this;
  }

  @Step("Заполнить Surname: {surname}")
  public ProfilePage fillSurname(String surname) {
    surnameInput.fill(surname);
    return this;
  }

  @Step("Выбрать страну: {country}")
  public ProfilePage selectLocation(String country) {
    location.click();
    countryOption(country).click();
    return this;
  }

  @Step("Загрузить аватар: {avatar}")
  public ProfilePage uploadAvatar(Path avatar) {
    avatarInput.setInputFiles(avatar);
    return this;
  }

  @Step("Нажать Save")
  public ProfilePage clickSave() {
    page.waitForResponse(this::isUpdateUser, save::click);
    return this;
  }

  @Step("Нажать Reset")
  public ProfilePage clickReset() {
    reset.click();
    return this;
  }

  @Step("Проверить имя пользователя в профиле: {username}")
  public ProfilePage checkThatUsername(String username) {
    assertThat(usernameInput).hasValue(username);
    return this;
  }

  @Step("Проверить First Name: {firstName}")
  public ProfilePage checkThatFirstName(String firstName) {
    assertThat(firstNameInput).hasValue(firstName);
    return this;
  }

  @Step("Проверить Surname: {surname}")
  public ProfilePage checkThatSurname(String surname) {
    assertThat(surnameInput).hasValue(surname);
    return this;
  }

  @Step("Проверить страну: {country}")
  public ProfilePage checkThatLocation(String country) {
    assertThat(location).hasText(country);
    return this;
  }

  @Step("Проверить src аватара: начинается с {srcPrefix}")
  public ProfilePage checkThatAvatarSrcStartsWith(String srcPrefix) {
    assertThat(avatarImage).hasAttribute("src", Pattern.compile("^" + srcPrefix + ".+"));
    return this;
  }

  private Locator countryOption(String country) {
    return page.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHas(
        page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName(country).setExact(true))
    ));
  }

  private boolean isUpdateUser(Response response) {
    String postData = response.request().postData();
    return response.ok()
        && response.url().contains("/graphql")
        && postData != null
        && postData.contains("\"operationName\":\"UpdateUser\"");
  }
}
