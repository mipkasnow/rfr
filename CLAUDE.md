# Rangiffler (rfr)

Монолит с моками: часть функций настоящая (регистрация, вход, профиль, дружба), часть возвращается из JSON-моков (фото, лайки, лента, статистика).

## Модули и порты

| Модуль | Что это | Порт | Инструкции |
|---|---|---|---|
| `rfr-auth` | Spring Authorization Server, схема `rangiffler-auth` | 9001 | [rfr-auth/CLAUDE.md](rfr-auth/CLAUDE.md) |
| `rfr-api` | GraphQL API `/graphql`, схема `rangiffler-api`; фото и лента — моки из `src/main/resources/mock/*.json` | 8081 | [rfr-api/CLAUDE.md](rfr-api/CLAUDE.md) |
| `rfr-gql-client` | React + Apollo, собирается отдельно от Gradle (`npm run dev`) | 3001 | [rfr-gql-client/CLAUDE.md](rfr-gql-client/CLAUDE.md) |
| `rfr-e2e` | E2E-тесты: JUnit 6, Playwright Java, Allure 3 | — | [rfr-e2e/CLAUDE.md](rfr-e2e/CLAUDE.md) |

MySQL — порт 3306, пользователь `root`, пароль `secret`.

## Окружение

- MySQL: `docker compose up -d --wait` или `bash localenv.sh` (тот же compose, остальные контейнеры не трогает). Сервисы: `./gradlew runAll` — `rfr-auth` и `rfr-api` параллельно. Фронт: `npm run dev` в `rfr-gql-client`.
- E2E: `./gradlew e2e` — тесты `rfr-e2e` с тегом `ui` против поднятого стенда. Задача `:rfr-e2e:test` без `--tests` этот тег исключает. Обе запускаются при каждом вызове.
- Нужны JDK 25, Node 22+, Docker.
- Инструкции модуля — в его `CLAUDE.md`. В Cursor тот же файл подключается при правках этого модуля.
