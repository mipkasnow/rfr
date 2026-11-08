## Неочевидное

- Настройки тестов — `TestConfig`: `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию. Ключи — в `rfr-e2e/README.md`.
- Page Objects — skill `rfr-page-objects`.
- Без комментариев и Javadoc. Вход только через UI, токены в localStorage не подставлять.
- Проход через Playwright MCP — не запуск теста. Подтверждение — `./gradlew :rfr-e2e:e2e --tests '<Тест>'`.
- Тесты с браузером помечены `@Tag("ui")`, иначе задача `e2e` их не запустит.
- Данные создаёт и удаляет `@User` → `UserExtension` → `UserFixture`. Широких `DELETE`/`TRUNCATE` нет.
- Второй пользователь в браузере — параметр `@Session @User UserSession` (или `@Session GuestSession` для гостя): `SessionExtension` открывает отдельный `BrowserContext`, входит через UI и закрывает контекст после теста; пользователя создаёт `UserExtension`. Контексты в тестах не создаются.
