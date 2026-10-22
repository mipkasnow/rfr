## Конфигурация

Настройки тестов — `TestConfig`: `-Drfr.<ключ>=…` или переменная `RFR_<КЛЮЧ>`, иначе значение по умолчанию. Подробности — `rfr-e2e/README.md`.

## Создание Page Objects

Используй скилл `rfr-page-objects` (`.claude/skills/rfr-page-objects/SKILL.md`).

## Архитектура проекта 

- Структура пакетов — `rfr-e2e/README.md`, раздел «Архитектура»: `config`, `jupiter`, `data`, `api`, `ui`, `assertions`, `test`. Новые классы кладутся в существующие пакеты.
- Page Objects и компоненты — по skill `rfr-page-objects` (`.claude/skills/rfr-page-objects/SKILL.md`).
- Без комментариев и Javadoc в коде `rfr-e2e` (Java, Gradle, SQL, properties): смысл передают имена и `@Step`.
- Вход в тестах только через UI; токены в localStorage не подставлять.
- Проход сценария через Playwright MCP — не запуск теста. Успех подтверждается JUnit-прогоном через Gradle.
- Тестовые данные создаются и удаляются фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE`/`TRUNCATE`.
