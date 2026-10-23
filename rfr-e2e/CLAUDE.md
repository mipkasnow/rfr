## Неочевидное

- Настройки тестов — `TestConfig`: `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию. Ключи — в `rfr-e2e/README.md`.
- Page Objects — skill `rfr-page-objects`.
- Без комментариев и Javadoc. Вход только через UI, токены в localStorage не подставлять.
- Проход через Playwright MCP — не запуск теста. Подтверждение — Gradle (`./gradlew e2e` или `:rfr-e2e:test`).
- Данные создаёт и удаляет `@User` → `UserExtension` → `UserFixture`. Широких `DELETE`/`TRUNCATE` нет.
