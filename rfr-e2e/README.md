# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UiTest` → `@UsePlaywright(BrowserOptions)`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер из `rfr.browser`, по умолчанию Chromium
./gradlew e2e                                             # все UI-тесты (тег ui)
./gradlew :rfr-e2e:e2e --tests '*MainPageTest'            # один тест
./gradlew :rfr-e2e:test                                   # всё без тега ui, стенд не нужен; с --tests тег ui не отсекается (запуск из IDE)
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
./gradlew :rfr-e2e:showTrace -Ptrace=/path/to/trace.zip   # открыть trace упавшего UI-теста в TraceViewer
```


Стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README).

Тесты, которым нужен браузер и стенд, помечены `@Tag("ui")` через `@UiTest` и запускаются задачей `e2e`. Задача `test` исключает тег `ui` и обходится без стенда. Обе задачи запускают тесты при каждом вызове, `--rerun-tasks` не нужен. Перед первым классом с тегом `ui` `StandReadinessExtension` ждёт MySQL, `rfr-auth`, `rfr-api` и фронт не дольше `rfr.standTimeout` секунд (по умолчанию 180). Ожидание включено по умолчанию; `rfr.standCheck=false` выключает его полностью, тесты без тега `ui` к стенду не обращаются.

## Конфигурация

`TestConfig` читает `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию. Gradle передаёт все `-Drfr.*` в тестовую JVM.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схема добавляется кодом) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` / `rfr.slowMo` | `chromium` / `true` / `0` (мс задержки действий Playwright) |
| `rfr.viewportWidth` / `rfr.viewportHeight` | `1920` / `1080` |
| `rfr.actionTimeout` / `rfr.navigationTimeout` | `5` / `30` (секунды) |
| `rfr.screenshotDir` | `src/test/resources/screenshots` — базовые скриншоты, путь от каталога `rfr-e2e` |
| `rfr.updateScreenshots` | `false` — режим обновления эталонов: нет эталона или он не совпал — записать текущий скриншот и пропустить тест, совпал — тест проходит без записи; обновлённые эталоны проверяют в pull request |
| `rfr.standCheck` / `rfr.standTimeout` | `true` / `180` (`false` выключает ожидание; без тега `ui` к стенду не ходят) |
| `rfr.migrationCheck` | `false` (задача `e2e` включает сверку) |
| `rfr.commit` | короткий хеш `HEAD` (передаёт Gradle, тест `git` не вызывает) |

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | всё про JUnit: `annotation` (`@User`, `@StaticUser`, `@Session`, `@ProbeTarget`, `@ProbeTest`, `@UiTest`), `extension` (`UserExtension` — создаёт пользователей для всех `@User` и отдаёт их по индексу параметра; `UserQueueExtension` — пул `@StaticUser`; `SessionExtension` — второй `BrowserContext` для `@Session`; `BrowserOptions` — настройки браузера и контекста, общие для основного контекста и сессий: размер окна из `TestConfig` и для стабильных скриншотов `deviceScaleFactor` 1, светлая схема, `reducedMotion`, локаль `en-US` и часовой пояс `UTC`; `BrowserTimeoutExtension`, `BrowserTraceExtension` — trace.zip только у упавших UI-тестов в Allure; `ScreenshotDiffExtension` — после теста находит все `ScreenshotMismatchError`, в том числе внутри `MultipleFailuresError` из `assertAll`, в suppressed и в причинах, и прикладывает каждую к Allure как image diff (`application/vnd.allure.image.diff`); `GqlStubsExtension` — единственный `GqlStubs` основного `BrowserContext` в Store, второй параметр `GqlStubs` — ошибка; через ServiceLoader: `AllureEnvironmentExtension` пишет Environment в Allure, `StandReadinessExtension` ждёт стенд, `StandSchemaExtension` сверяет миграции — Allure первым, чтобы `afterSuite` сработал даже после падения чужого `beforeSuite`), `condition` (`ProbeTargetCondition` — мишени probe-тестов выполняются только внутри EngineTestKit; `@ProbeTarget` ставит тег `probe-target`, который задачи `test` и `e2e` исключают), `probe` |
| `data` | тестовые данные и их владельцы без JUnit; `data/fixture` — `UserFixture` с реализацией `SingleUserFixture`, `StaticUserFixture` и декоратором `UserWithRelationsFixture` (`Relation`: друг, входящая и исходящая заявка), `UserRequest` собирает их цепочку; `MockPhoto` — фото mock-ленты `rfr-api` с ожидаемыми страной, описанием, владельцем и числом лайков; `db` — `Databases`, `Jdbc` и неизменяемые объекты строк (`Row`/`Rows`: `AuthAccount`, `ApiProfile`, `Friendship`, `Friendships`), которые прячут весь SQL за `create`/`delete`/`exists`; `Migrations` сверяет схему через Flyway validate и отдаёт текущую версию для Allure |
| `domain` | `enum` предметной области с текстами UI: `Notification`, `PeopleAction`, `FriendStatus` (`labels()` — тексты ячейки действий) — без Playwright, JUnit и SQL |
| `network` | сетевой транспорт фронта для тестов: `GqlStubs` регистрирует `GqlOperation` на `/graphql` и снимает их в `close()`; `StubbedOperation` / `HeldOperation` / `GqlStubs.error` / `AwaitedGqlOperation` / `PatchedOperation` (`shouldPatchBody` — частичная подмена ответа по JSON Path через Jayway JsonPath 3 + Jackson 3); package-private `GqlRequest` (Jackson 3). Используется только в тестах, не в `ui` |
| `session` | дополнительные браузерные сессии и их фикстуры, без JUnit и SQL; знает `data`, `ui`, `config`, `network`, но не `jupiter`. У `GuestSession` / `UserSession` есть `stubs()` — непустой `Optional` при `@Session(useStubs = true)`. Контекст создаёт `SessionExtension`, закрывает фикстура из `session/fixture`; в тест уходит record сессии, а не фикстура |
| `visual` | инфраструктура скриншотных тестов: `BaselineStore` — базовые скриншоты одного окружения в `<rfr.screenshotDir>/<ОС>/<браузер>/<имя>.png`; `current()` берёт браузер из `TestConfig` и определяет ОС (`macos`, `windows`, `linux`), `read` отдаёт PNG или пустой `Optional`, если базового скриншота ещё нет, `write` записывает PNG и создаёт каталоги; `ScreenshotAssertions` — сравнивает PNG с базовым скриншотом из `BaselineStore` через `image-comparison` с допуском в процентах изменённых пикселей (по умолчанию 0): без эталона прерывает тест через `MissingBaselineException`, при другом размере или различии сверх допуска бросает `ScreenshotMismatchError`; при `rfr.updateScreenshots=true` вместо этого записывает текущий скриншот как эталон и прерывает тест через `BaselineWrittenException` (opentest4j `TestAbortedException`), а совпавший эталон не трогает; `ScreenshotMismatchError` (`AssertionError`) — падение скриншотного теста, несёт `ScreenshotDiff` — имя и PNG эталона, текущего скриншота и разницы; `MissingBaselineException` (opentest4j `TestAbortedException`) — прерывание теста, например без базового скриншота; сравнение изображений — библиотека `com.github.romankh3:image-comparison`; без JUnit Jupiter API, SQL и локаторов |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `ui` | `page` — Page Objects (`PeoplePage` / `PeopleTab`), `component` — общие части страниц (`PeopleTable`, `PeopleRow`, `Snackbar`, `PhotoCard`) |
| `test` | только тесты, по фичам: `auth`, `profile`, `people` (`PeopleLogin`, `PeopleLoaderTest`, `FriendshipActionsTest`, `FriendshipActionErrorTest`), `travels` (`TravelsLogin`, `MyTravelsMockTest`, `MyTravelsVisualTest`, `PhotoDeletionTest`, `FeedLikesPatchTest`), … |

Правила: в `jupiter` нет SQL и локаторов, расширение собирает объекты из `config`, `stand`, `data`, `session`, `network`, `visual`; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`).

Доказательство корректности теста - запуск gradle таски.
