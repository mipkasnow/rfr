# Язык

Все summary, заголовки findings, тела комментариев, объяснения и предложения фиксов — только на русском.
Код, пути файлов, имена классов/методов и цитаты из diff не переводить.

# Контекст проекта

Учебный монолит Rangiffler (`rfr`): `rfr-auth`, `rfr-api`, `rfr-gql-client`, `rfr-e2e`.
Фокус ревью — изменения в `rfr-e2e` и связанные skill/MCP-конфиги.
Ориентиры по структуре и контрактам:

- `CLAUDE.md`
- `rfr-e2e/README.md` (архитектура пакетов)
- `.claude/skills/rfr-page-objects/SKILL.md`
- `docs/lessons/01/homework/README.md`

# Архитектура rfr-e2e

Пакеты: `config`, `jupiter`, `data`, `utils`, `ui` (`page` / `component`), `test`.
Новые классы класть только в существующие пакеты.
В `test` нет локаторов и SQL; в `jupiter` нет SQL и локаторов; в `data` нет JUnit.
Состояние теста — через Store расширения, не через `ThreadLocal`.

# Page Objects (`ui/page`, `ui/component`)

Ревьюить по контракту skill `rfr-page-objects`:

- класс `final`; поля `private final`; конструктор только принимает `Page` и присваивает поля
- композиция компонентов вместо наследования; базовых page-классов нет
- публичные методы с `@Step`, параметры попадают в текст шага
- действие отдельно от проверки: `fill…`/`click…` не проверяют; `checkThat…` только через `PlaywrightAssertions` и возвращают `this`
- локаторы: `getByRole` / `getByLabel` / `getByPlaceholder` / `getByText` / `getByTestId`; без `first()`/`nth()`, MUI-классов и XPath
- MUI Select — `combobox`; пункт страны через `OPTION` с фильтром по `IMG` с точным именем страны
- если следующий шаг обгоняет запрос, `waitForResponse` живёт внутри метода действия, не в тесте и не в `checkThat…`
- без комментариев и Javadoc в коде `rfr-e2e`

# Тесты и данные

- вход только через UI; не подставлять `access_token` / `id_token` в `localStorage`
- тестовые данные создавать и удалять фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE`/`TRUNCATE`
- флагивать гонки UI/async (upload файла, GraphQL мутации) и тавтологичные проверки локаторов

# Приоритет замечаний

Сообщать только о реальных багах, хрупких локаторах, нарушениях архитектуры/skill и рисках флаков.
Не предлагать рефакторинг «на вкус», лишние абстракции и стилевые ниты вне правил курса.
