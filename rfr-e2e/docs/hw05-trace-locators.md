# Пункт 4: TraceViewer и замеры локаторов

## Красная проверка вкладки People

1. В `PeoplePage.openTab` временно имя вкладки заменено на `Broken {title}`.
2. `./gradlew e2e --tests '*IncomeInvitationsTabTest.listsOnlyInviters' -Drfr.actionTimeout=3` — TimeoutError на ожидании вкладки.
3. В `build/allure-results` у упавшего теста attachment `trace.zip`.
4. Открытие: `./gradlew :rfr-e2e:showTrace -Ptrace=rfr-e2e/docs/hw05-failed-tab-trace.zip`.
5. Скриншот шага ожидания `Broken Income invitations`: `hw05-trace-wait-step.jpg` (People открыт, вкладка не найдена).
6. Правка локатора откачена через `git revert`.

## Таблица Pick locator (фильтр Netherlands активен, список стран закрыт)

| Локатор | 1920 | 390 |
|---|---|---|
| `getByRole(BUTTON, "Country filter: Netherlands", exact)` | 1 | 1 |
| `getByRole(BUTTON, "Reset country filter", exact)` | 1 | 1 |
| `getByRole(BUTTON, "countries visited")` (список закрыт) | 1 | 1 |
| `getByRole(BUTTON, "countries visited")` (список открыт) | 0 | 0 |

Этим элементам test-id не нужен: у каждого уже уникальное доступное имя (или стабильная подстрока имени), одинаково на широкой и узкой раскладке.
