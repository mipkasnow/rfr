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

- Поднимай MySQL (`docker compose up -d`), сервисы (`./gradlew runAll` — `rfr-auth` и `rfr-api` параллельно) и фронт.
- E2E-тесты: `./gradlew e2e --rerun-tasks` — все тесты `rfr-e2e`; без `--rerun-tasks` Gradle пропускает неизменившийся прогон.
- Нужны JDK 25, Node 22+, Docker.
