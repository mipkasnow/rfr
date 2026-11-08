# План: второй BrowserContext в тесте (`@Session`)

Ветка: `v3/l06-pw-contexts-live`. Дата: 2026-11-02.

## Цель

Тест получает дополнительную браузерную сессию — отдельный `BrowserContext` со своими cookies и localStorage — рядом с основной `Page` от `@UsePlaywright`. Сессия бывает гостевой или с пользователем; сессия с пользователем приходит в тест уже после входа через UI.

```java
@Test
void bobSeesInvitationFromAlice(Page page, @User TestUser alice,
                                @Session @User UserSession bob) {
  // alice — основная Page, как сейчас; bob — второй контекст, уже на MainPage
  bob.mainPage().clickPeople()...;
}

@Test
void guestSeesLanding(Page page, @Session GuestSession guest) {
  guest.landingPage().clickLogin()...;
}
```

## Принятые решения

| # | Решение |
|---|---|
| 1 | Два типа сессии вместо `Optional`: `GuestSession` и `UserSession`. Наличие пользователя проверяет компилятор. |
| 2 | Параметр: `@Session` для гостя, `@Session @User(...)` для сессии с пользователем. Всех пользователей создаёт только `UserExtension` — для каждого параметра с `@User`, независимо от типа. `SessionExtension` пользователей не создаёт, а берёт готового по индексу параметра. |
| 3 | `SessionExtension` создаёт контексты в `beforeTestExecution`: к этому моменту все `beforeEach`, в том числе `UserExtension`, гарантированно завершены, независимо от порядка регистрации extension'ов. |
| 4 | Фикстуры сессий — `session/fixture`, остальное — `session`. |
| 5 | Основная `Page` и вход основного пользователя не меняются. |
| 6 | Таймауты второго контекста — из того же кода, что у основного. Trace второго контекста пока не пишем. |
| 7 | `UserSession.mainPage()` отдаёт `MainPage` сразу после входа; повторный вызов отдаёт тот же объект, без перехода. |

## Детали решений

**`UserExtension`.**
- `beforeEach` создаёт пользователя для каждого параметра с `@User`, как сейчас: `UserRequest(user).fixture()`, `put` в Store до `create()`.
- Новый `public static TestUser user(ExtensionContext context, int index)` — только чтение фикстуры из Store по индексу; нет фикстуры — `ParameterResolutionException` с индексом и методом. Вызывают `resolveParameter`, `SessionExtension` и подставное расширение пробы `UserExtensionTest.UserTakenByIndex` — поэтому метод публичный, а не package-private. **Сделано.**
- `supportsParameter` по-прежнему только `TestUser`.
- Global-правило «без static-методов» здесь уступает прямому решению пользователя и совпадает с образцом Playwright JUnit (`BrowserContextExtension.getOrCreateBrowserContext`).

**Таймауты и настройки контекста.**
- `BrowserTimeoutExtension` получает package-private `static void apply(BrowserContext context)` с текущим телом; его `beforeEach` вызывает `apply(getOrCreateBrowserContext(context))`.
- Настройки нового контекста — `new BrowserOptions().getOptions().contextOptions` (публичное поле `Options`): тот же viewport.
- `Browser` — `com.microsoft.playwright.impl.junit.BrowserExtension.getOrCreateBrowser(context)`: внутренний API Playwright, соседний `BrowserContextExtension` уже используем.

## Состав

### `jupiter/annotation`
- `@Session` — `@Target(PARAMETER)`, `@ExtendWith(SessionExtension.class)`, без атрибутов.

### `jupiter/extension`
- `SessionExtension implements BeforeTestExecutionCallback, ParameterResolver`.
  - `beforeTestExecution`: для каждого параметра с `@Session` проверить сочетание типа и `@User`:
    - `GuestSession` без `@User` — гостевая сессия;
    - `UserSession` с `@User` — сессия пользователя `UserExtension.user(context, index)`;
    - иначе — понятная ошибка (`UserSession` без `@User`, `GuestSession` с `@User`, чужой тип); контекст не создаётся. Пользователя для `GuestSession` с `@User` уже создал `UserExtension` — его удалит Store.
  - Создать контекст `browser.newContext(options)`, `BrowserTimeoutExtension.apply(...)`, сразу обернуть в фикстуру, `put` в Store с ключом по индексу параметра, затем `create()`.
  - `supportsParameter`: тип `GuestSession` или `UserSession`, `@Session`, параметр текущего тестового метода (как в `UserExtension`).
  - `resolveParameter`: `fixture.session()` из Store; нет фикстуры — `ParameterResolutionException`.
- `UserExtension`: `user(context, index)` только на чтение; `resolveParameter` через него.
- `BrowserTimeoutExtension`: `apply(BrowserContext)`.

### `session`
- `record GuestSession(LandingPage landingPage)`.
- `record UserSession(TestUser user, MainPage mainPage)`.

### `session/fixture`
- `interface SessionFixture<T> extends AutoCloseable { T session(); void create(); void close(); }`.
- `GuestSessionFixture(BrowserContext context)`: `create()` — `newPage()` и `LandingPage.open()`; `close()` — `context.close()`.
- `UserSessionFixture(BrowserContext context, TestUser user)`: `create()` — вход через UI `LandingPage → LoginPage → MainPage`, запоминает `MainPage`; `session()` до `create()` — `IllegalStateException`; `close()` — `context.close()`. Пользователя не удаляет: им владеет `UserExtension`.

Зависимости: `session` знает `data`, `ui`, `config`; не знает `jupiter`. `jupiter` знает `session`.

### Порядок закрытия
`close()` фикстуры сессии закрывает только контекст, пользователя закрывает Store `UserExtension`. Порядок между ними не критичен: закрытый контекст не мешает удалить пользователя, удалённый пользователь не мешает закрыть контекст. Пробы ниже подтверждают, что после теста нет ни контекста, ни строк.

## Правки правил

- `rfr-test-review`:
  - правило 11 — **уже уточнено**: пользователей создаёт только `UserExtension` для любого `@User`, другие extension'ы берут их по индексу не раньше `beforeTestExecution`, `@User` на неразрешённом типе не оставляет строк;
  - правило 6 — `Page` и `BrowserContext` основной сессии принадлежат `@UsePlaywright`; дополнительный контекст создаёт только `SessionExtension` и сразу отдаёт фикстуре из `session/fixture`, которая его закрывает; тесты контексты не создают и не закрывают;
  - правило 5 — `@User` на `UserSession` тоже один параметр и один ключ Store; пользователя создаёт `UserExtension`, сессия берёт его по индексу;
  - правило 7 — `session` не зависит от `jupiter`, в `session` нет SQL;
  - правило 14 — фикстуры тестовых данных живут в `data/fixture`, фикстуры сессий — в `session/fixture`; тест получает record из `data` или `session`, типы из `*/fixture` в тест не попадают.
- `rfr-e2e/README.md`, «Архитектура»: строка пакета `session`, `@Session` и `SessionExtension` в строке `jupiter`.
- `rfr-e2e/CLAUDE.md`: строка про `@Session` рядом с правилом о фикстурах.

## Шаги (TDD)

Пробы — в `jupiter/probe`. Мишени с браузером — top-level package-private классы с `@UiTest @ProbeTarget`, внешний класс пробы без браузера: Playwright JUnit ищет `@UsePlaywright` только на внешнем классе и держит контекст в `ThreadLocal`.

1. **Пробы `UserExtension`** (`UserExtensionTest`, браузер не нужен) — **сделано**. Мишень `UnresolvedType` с `@User String` и подставным `UserTakenByIndex`, которое в `beforeTestExecution` берёт пользователя через `UserExtension.user(context, 0)` и публикует его в отчёт:
   - `@User String` валит тест `ParameterResolutionException`;
   - другое расширение получает созданного пользователя по индексу, аккаунт существует;
   - после теста аккаунта нет;
   - существующие мишени остаются зелёными: поведение `beforeEach` не меняется.
2. `UserExtension.user(context, index)` — чтение по индексу, `resolveParameter` через него — **сделано**. `UserExtensionTest`: 23 из 23.
3. `BrowserTimeoutExtension.apply` — рефакторинг без изменения поведения; `ProfileTest` зелёный — **сделано**, 5 из 5.
4. **Пробы `SessionExtension`** (`SessionExtensionTest`, красные до шага 5):
   - гостевая сессия: контекст отличается от основного, открыт `LandingPage`, `getByTestId` работает, после теста контекст закрыт;
   - сессия пользователя: пользователь существует (`exists()` из `db`) во время теста и удалён после, `mainPage()` авторизован (`checkThatMainElementsVisible`);
   - сессия пользователя и `@User TestUser` в одном тесте: два разных пользователя, оба удалены после теста;
   - упавший тест: контекст закрыт, пользователь удалён;
   - `UserSession` без `@User`: понятная ошибка, контекста нет;
   - `GuestSession` с `@User`: понятная ошибка, контекста нет, после теста строк пользователя нет;
   - тест без `@Session` под `SessionExtension` на классе: ничего не создаётся (правило 11).
   **Сделано.** Мишени — пары упорядоченных тестов: второй тест считает открытые контексты браузера, так закрытие сессии видно без опоры на порядок callback'ов. `GuestSession` с `@User` использует `UserExtensionTest.UserTakenByIndex` на методе, а не на классе.
5. `@Session`, `SessionExtension`, `session`, `session/fixture`. Пробы шага 4 зелёные — **сделано**: `SessionExtensionTest` 16 из 16, `UserExtensionTest` 23 из 23.
6. E2E-тест двух пользователей в `test/people`: основной пользователь отправляет приглашение, второй в `@Session @User UserSession` видит его во вкладке входящих — **сделано**: `FriendshipFlowTest` проходит весь флоу Add → Accept → Friends у обоих → Remove → пусто у обоих; три прогона подряд зелёные. В Page Objects добавлены `PeopleRow.clickAccept`, `PeopleRow.clickRemove`, `PeoplePage.reload`, `PeoplePage.checkThatNotification`. Позже сценарий заменён пятью атомарными тестами `FriendshipActionsTest`, а `PeoplePage.reload` удалён — см. `docs/plans/2026-11-02-atomic-friendship-tests.md`.
7. Правки skill'ов (правила 5, 6, 7, 14), README, `rfr-e2e/CLAUDE.md` — **сделано**. Дополнительно: компонент `ui/component/Snackbar`, правило 15 skill `rfr-page-objects` о текстах UI в `enum`.

Проверка: `./gradlew :rfr-e2e:test --tests '*UserExtensionTest' --tests '*SessionExtensionTest'` и `./gradlew :rfr-e2e:e2e --tests '<новый тест>'`. Запуск — после согласования.

## Риски и открытые вопросы

- `BrowserExtension.getOrCreateBrowser` — внутренний API Playwright.
- **Ограничение Playwright JUnit (issue не создаём):** Playwright JUnit 1.63 держит `Playwright` в статическом `ThreadLocal` и не очищает его, когда реестр в корневом Store закрывает экземпляр. Второй прогон EngineTestKit в том же потоке падает с `Playwright connection closed`. Обход только в пробе: `SessionExtensionTest.run` выполняет каждый прогон в своём потоке с таймаутом 3 минуты. Убрать обход после исправления в Playwright.
- `testIdAttribute` из `BrowserOptions` Playwright задаёт для всего экземпляра Playwright; проба гостевой сессии подтверждает, что `getByTestId` работает и во втором контексте.
- Таймауты второго контекста напрямую не наблюдаются: `BrowserContext` не отдаёт их значения. Подтверждение — по чтению кода.
- `@User` на неразрешённом типе создаёт пользователя, которого Store сразу удаляет; ошибку выдаёт JUnit, а не `UserExtension`.
- Вход второго пользователя в `beforeTestExecution` попадает в шаги Allure перед телом теста; если это мешает отчёту, вынести шаги в отдельный контейнер.
- Trace второго контекста отложен; при падении во втором окне в Allure будет только trace основного.
