# ДЗ 08 п.2 — аудит доступности `rfr-gql-client`

## Промпт агенту
Обойди экраны Rangiffler через Playwright MCP под пользователем стенда и прочитай
код компонентов. Экраны: шапка и боковое меню, My travels (лента, фильтр, карта,
диалог добавления/редактирования фото), People (вкладки, таблица, compact ≤900px),
профиль, уведомления. Login/register (`rfr-auth`) вне задачи. PhotoCard и строки
People уже исправлены на занятии — не считать. Критично: нет имени у control,
неверная/отсутствующая роль, состояние не сообщается, `aria-labelledby` /
`aria-controls` / `aria-describedby` на несуществующий id, диалог без имени,
фокус вне диалога. Иначе — некритично.

Пользователь стенда: `rfr_a11y08aa` (зарегистрирован в MCP).

## Таблица находок агента

| # | Экран | Компонент / строка | Что не так | Доказательство | Кого задевает | Уровень |
|---|---|---|---|---|---|---|
| A1 | My travels → Add photo | `DialogContext.tsx` ~161: `aria-describedby="alert-dialog-slide-description"` | `aria-describedby` ссылается на несуществующий id | MCP `evaluate`: `describedbyExists: false`; в DOM keepMounted-диалог всегда с этим атрибутом | Скринридер получает битую ссылку описания диалога | **критично** |
| A2 | People | `TabPanel/index.tsx` `aria-labelledby={simple-tab-${value}}`; `PeoplePage` Tab без `id` | `aria-labelledby` tabpanel → нет элемента `simple-tab-friends` и т.п.; у Tab нет `id`/`aria-controls` | MCP: `labelledbyExists: false`, у tabs `id: ""` | Скринридер не связывает панель с выбранной вкладкой | **критично** |
| A3 | Profile (+ любой экран с DialogProvider) | `ImageUpload/index.tsx` `id="image__input"` | Один и тот же id у file input в диалоге (keepMounted) и в профиле | MCP profile: два `INPUT:file` с id `image__input` | `label[for=image__input]` и `getElementById` указывают на первый input (скрытый в диалоге); загрузка аватара с клавиатуры/скрином ломается | **критично** |
| A4 | My travels | `PhotoModal/index.tsx` `aria-describedby="modal-modal-description"` | Тот же класс бага, что A1 | Код; компонент в дереве приложения не монтируется (используется Dialog) | — | некритично / мёртвый код |
| A5 | Шапка | `MenuAppBar` кнопка `open drawer` без `aria-expanded` | Состояние сайдбара не озвучивается | Snapshot: `button "open drawer"` без expanded | Скринридер не знает, открыт ли drawer | некритично (permanent drawer, навигация по ссылкам есть) |
| A6 | My travels | `VisitedCountries` Button: есть `aria-expanded`, нет `aria-controls` | Список стран в Tooltip не привязан id | Код + snapshot кнопки | Ухудшает объявление попапа | некритично |
| A7 | ImageUpload | `<img>` превью без `alt` при выбранном файле | Нет альтернативного текста | Код `ImageUpload` | Скринридер не описывает превью | некритично (есть кнопка Upload с именем) |

## Проверка находок (итог)

| # | Итог | Почему |
|---|---|---|
| A1 | **подтверждена** | MCP evaluate на открытом диалоге; строка в `DialogContext.tsx` |
| A2 | **подтверждена** | MCP evaluate на `/people`; `TabPanel` + отсутствие id у `Tab` |
| A3 | **подтверждена** | MCP evaluate на `/profile`: два элемента с одним id |
| A4 | **вычеркнута** | `PhotoModal` не рендерится; живой путь — Dialog. Черновик issue |
| A5 | **понижена → некритично** | Задача экрана (переход My map / People / Profile) выполняется ссылками сайдбара |
| A6 | **некритично** | Кнопка имеет имя и `aria-expanded`; фильтр по стране доступен и с карты |
| A7 | **некритично** | Не блокирует загрузку; кнопка Upload именована |

Где агент ошибся бы без проверки: A4 легко завысить до критичного по коду без проверки, что компонент не на экране.

## Исправления (коммиты)

| # | Коммит | Сообщение |
|---|---|---|
| A1 | `505ed0f` | fix(gql-client): убрать битый aria-describedby у диалога фото |
| A2 | `803c403` | fix(gql-client): связать вкладки People с tabpanel через id и aria-controls |
| A3 | `afc3897` | fix(gql-client): уникальный id у file input ImageUpload |

После исправлений: bump `rfr-gql-client` 0.1.0→0.2.0, ARIA-тесты Add photo / People tabs, красная проверка откатом A2.

## Черновики issue (некритичные)

1. **PhotoModal: мёртвый `aria-describedby`.** Удалить атрибут или добавить description, либо выпилить неиспользуемый `PhotoModal` в пользу Dialog.
2. **MenuAppBar: `aria-expanded` у кнопки drawer.** Сообщать состояние `sidebarState`.
3. **VisitedCountries: `aria-controls` на список стран.** Привязать id панели Tooltip/List.
4. **ImageUpload: `alt` у превью.** Задать `alt` из description/«Selected image».

## Выбор мест для ARIA-тестов

1. Диалог Add photo — главная задача создания фото; A1 ломает описание диалога и легко вернуть копипастой MUI-примера.
2. People tabpanel — главная задача вкладок People; A2 ломает связку tab↔panel.
3. (Опционально третий) не дублируем ImageUpload snapshot: уникальный id проверяется косвенно тем, что profile upload остаётся рабочим; фиксируем двумя тестами выше.

Остальные исправления без теста: A3 покрыт уникальным `useId` — регрессия id менее вероятна при правке разметки диалога, чем повторный copy-paste `aria-describedby`/`simple-tab-*`.
