# Пункт 3: Trace второго BrowserContext

## Красная проверка шага сессии Bob

1. В `SessionIsolationTest` итоговая проверка сессии временно заменена на `checkThatEmpty()` вместо `checkThatPeople` с другом.
2. Упавший тест в Allure даёт два вложения: `trace.zip` (основной контекст) и `Playwright trace (session 2)`.
3. Копии: `hw06-main-trace.zip`, `hw06-session-trace.zip`.
4. Открытие: `./gradlew :rfr-e2e:showTrace -Ptrace=$PWD/rfr-e2e/docs/hw06-session-trace.zip`.
5. Скриншот шага ожидания: `hw06-session-trace-wait.png` (Assert `isVisible` «There are no users yet», на Friends виден друг).
6. Правка теста откачена через `git revert`.
