# План: GraphQL subscription на изменения дружбы

Дата: 2026-11-08. Задача ДЗ 06 п.4*. Код подписок не реализуется — только требования, промпт, техплан и сверка.

Сейчас наблюдатель в e2e открывает People **после** действия актора, потому что открытая вкладка не узнаёт об изменениях. Цель продукта: наблюдатель уже на People видит строку/кнопки и snackbar **без перезагрузки**.

---

## 1. Функциональные требования

Контекст для разработчика, который не видел приложение.

**Продукт.** Rangiffler: соцсеть путешествий. Дружба и заявки — настоящие (БД `rangiffler-api`). Страница People: вкладки Friends, All People, Outcome invitations, Income invitations; поиск; пагинация. Действия дружбы идут mutation `friendship(input: { user, action })`, где `action` ∈ `ADD | ACCEPT | REJECT | DELETE`. Отмена исходящей заявки — `DELETE` со стороны отправителя по `PENDING`. Уведомления актору уже есть (snackbar): Invitation sent / accepted / declined / cancelled, Friend deleted.

**Вне рамок.** Лента, фото, лайки, профиль, статистика, страны. Подписки только про дружбу/заявки и UI People (+ snackbar).

### 1.1. События

Каждое успешное изменение дружбы порождает одно логическое событие. Неуспешные mutation (ошибка, конфликт) событий не шлют.

| Код | Когда | Актор (кто нажал) | Контрагент |
|---|---|---|---|
| `INVITATION_SENT` | `ADD` создал `PENDING` | отправитель | адресат |
| `INVITATION_ACCEPTED` | `ACCEPT` сделал `FRIEND` | адресат | отправитель |
| `INVITATION_DECLINED` | `REJECT` снял `PENDING` | адресат | отправитель |
| `INVITATION_CANCELLED` | `DELETE` снял `PENDING` со стороны отправителя | отправитель | адресат |
| `FRIEND_DELETED` | `DELETE` снял `FRIEND` с любой стороны | удаливший | бывший друг |

Стороны события: **актор** и **контрагент**. Оба должны иметь возможность узнать о событии в своих сессиях (см. получатели).

### 1.2. Получатели

- Событие получает только **вошедший** пользователь и только если он **актор или контрагент** этой пары. Чужие заявки/дружбы не приходят.
- Гость (не залогинен) подписку не держит и событий не видит.
- Тот же пользователь в **нескольких окнах/вкладках/BrowserContext** (два входа одним username): **все** его активные подписки получают событие (связка с изоляцией контекстов из п.2 ДЗ: cookies разные, identity один).
- Другие пользователи, даже друзья актора/контрагента, событие не получают.

### 1.3. Экран People и snackbar

Обновление списка и snackbar относятся к **получателю события** в каждом его окне, где открыт фронт после входа.

**Snackbar (дополнение к текущему поведению актора).**

Сейчас snackbar видит в основном тот, кто нажал. Требование: **контрагент тоже видит snackbar** по тому же событию, иначе принятие/удаление «молчаливые». Тексты для контрагента (новые или те же формулировки — продуктово зафиксировать в UI-копирайте; ниже — целевой смысл):

| Событие | Актор (как сейчас / оставить) | Контрагент |
|---|---|---|
| `INVITATION_SENT` | Invitation sent | Invitation received |
| `INVITATION_ACCEPTED` | Invitation accepted | Friend added (или тот же Invitation accepted — главное, snackbar есть) |
| `INVITATION_DECLINED` | Invitation declined | Invitation declined |
| `INVITATION_CANCELLED` | Invitation cancelled | Invitation cancelled |
| `FRIEND_DELETED` | Friend deleted | Friend deleted |

Snackbar показывается, даже если пользователь **не** на People (например на ленте/профиле): достаточно глобального snackbar. Список People обновляется только если открыт People (или его данные уже в клиентском кэше активной страницы People).

**Строка в таблице — по вкладкам для контрагента (и для второго окна актора):**

| Событие | Вкладка, где строка должна измениться | Что видно |
|---|---|---|
| `INVITATION_SENT` | у адресата: Income; у отправителя: Outcome (и All People: статус/кнопки) | появляется заявка / кнопки Accept·Decline или Cancel |
| `INVITATION_ACCEPTED` | Friends у обоих; Income/Outcome очищаются от этой пары; All People — статус Friend / Remove | строка дружбы; заявка исчезает |
| `INVITATION_DECLINED` | Income у адресата и Outcome у отправителя — строка исчезает; All People — снова Add | нет заявки |
| `INVITATION_CANCELLED` | Outcome у отправителя и Income у адресата — строка исчезает | нет заявки |
| `FRIEND_DELETED` | Friends у обоих — строка исчезает; All People — снова Add | нет дружбы |

**Если открыта «не та» вкладка People.** Событие всё равно доставляется; snackbar показывается. Активная вкладка обновляется только если затронутая строка к ней относится. Соседние вкладки при следующем открытии/переключении не должны показывать устаревшую строку (инвалидация кэша запросов People или точечный update).

**Если открыта не People.** Только snackbar; при следующем заходе на People данные свежие (через refetch при mount или актуальный кэш после subscription).

**Поиск.** Если в поле поиска строка **не** проходит фильтр — в текущем списке она не появляется и не «ломает» поиск. Если проходит — поведение как без поиска.

**Пагинация.** Если после события пользователь должен появиться на **другой** странице, чем сейчас открыта — текущую страницу не прыгать; строка не обязана появиться на чужой странице. Если пользователь **уже на текущей странице** и должен исчезнуть — строка исчезает без перезагрузки. Дубликатов одной и той же строки на странице быть не должно.

### 1.4. Доступ, выход, токен, обрыв

- Подписка только с валидным access token; без `read` (или принятой для subscription политики) — отказ.
- **Logout** (п.2 ДЗ): подписка закрывается; после выхода событий не видно; повторный вход — новая подписка. Окно, где пользователь ещё вошёл, продолжает получать события (изоляция контекстов).
- **Истечение / 401 токена:** подписка обрывается; поведение как при текущем 401 на HTTP (сессия чистится, редирект на лендинг) — без «тихих» событий после инвалидации.
- **Обрыв сети и reconnect:** push-события, случившиеся **во время** обрыва, **не гарантируются** повторной доставкой. После восстановления соединения клиент делает **один refetch** текущих People-запросов (если страница People активна) и заново подписывается. Так закрывается дыра «молча устарели», без очереди missed events на бэке.

### 1.5. Наблюдаемые ограничения

- От успешного ответа mutation у актора до snackbar и обновления списка у контрагента в уже открытом People — **не больше 3 секунд** на локальном стенде без искусственных задержек.
- Дубли строк одного username на вкладке недопустимы.
- Перестановка порядка списка допустима, если не нарушает текущие правила сортировки API; e2e проверяют наличие/отсутствие и кнопки, не стабильный индекс.

### 1.6. Критерии приёмки (будущие e2e, две сессии)

Общий шаблон: оба вошли; **наблюдатель уже открыл нужную вкладку People**; затем актор делает одно действие; наблюдатель **без reload** видит snackbar и итоговую проверку списка.

1. **Заявка.** Alice на Outcome (или All People); Bob уже на Income. Alice Add → Bob ≤3с: snackbar Invitation received + строка Alice с Accept/Decline.
2. **Принятие.** Alice уже на Outcome (или Friends пустой/без Bob); Bob на Income Accept → Alice ≤3с: snackbar + Bob в Friends / заявка пропала.
3. **Отклонение.** Alice уже на Outcome; Bob Decline → Alice ≤3с: snackbar Invitation declined + пустой Outcome по этой заявке.
4. **Отмена.** Bob уже на Income; Alice Cancel → Bob ≤3с: snackbar Invitation cancelled + пустой Income.
5. **Удаление дружбы (инициатор).** Bob уже на Friends; Alice Remove → Bob ≤3с: snackbar Friend deleted + нет Alice в Friends.
6. **Удаление дружбы (вторая сторона).** Симметрично: наблюдатель — тот, кого удалили; то же.
7. **Два окна одного пользователя.** Alice в контексте A и B, оба на Friends с Bob. Bob Remove → **оба** окна Alice ≤3с: snackbar + пустой список по Bob.
8. **Logout одного окна.** Alice в A и B; A делает Logout. Bob шлёт заявку Alice. Окно B получает событие; окно A (гость) — нет.

---

## 2. Промпт агенту

```text
Контекст: монолит Rangiffler (rfr-api Spring GraphQL + JWT, rfr-gql-client Apollo Client HTTP-only).
Дружба: Mutation.friendship, FriendshipAction ADD|ACCEPT|REJECT|DELETE, одна строка friendship на пару.
Подписок в схеме сейчас нет. Websocket/subscription transport не настроен.

Ниже — функциональные требования из docs/plans/2026-11-08-friendship-subscriptions.md §1
(события, получатели, UI/snackbar у контрагента, поиск/пагинация, multi-window,
logout/token/reconnect с refetch без гарантии missed push, SLA 3с, вне рамок лента/фото,
критерии приёмки e2e).

Составь план изменений ТОЛЬКО бэка и фронта: шаги, какие файлы/слои трогать,
схема GraphQL (Subscription + payload), как публиковать из UserService после успешной mutation,
безопасность subscription, Apollo split link (HTTP + WS), где слушать на клиенте,
как обновлять кэш/списки People и показывать snackbar контрагенту.
Для каждого шага — как проверить (ручная проверка или опора на критерии §1.6).
Ничего не реализуй в коде. Не расширяй scope на ленту/фото. Если чего-то нет в требованиях —
явно пометь «не в требованиях, предложение» и не делай это обязательным шагом.
```

---

## 3. Техплан агента (бэк + фронт)

### 3.1. Схема GraphQL (`rfr-api/.../graphql`)

1. Добавить:

```graphql
type Subscription {
  friendshipEvents: FriendshipEvent!
}

type FriendshipEvent {
  type: FriendshipEventType!
  counterparty: User!   # второй участник относительно текущего подписчика
  at: Date!
}

enum FriendshipEventType {
  INVITATION_SENT
  INVITATION_ACCEPTED
  INVITATION_DECLINED
  INVITATION_CANCELLED
  FRIEND_DELETED
}
```

2. Payload **персонализирован**: один и тот же факт дружбы уходит двумя сообщениями (или одним publish с фильтрацией по userId) — подписчик видит `type` + `counterparty` с своей точки зрения.
3. Проверка: схема поднимается, introspection видит `Subscription`.

### 3.2. Транспорт и security (`rfr-api`)

1. Включить WebSocket для Spring for GraphQL (graphql-ws), endpoint рядом с `/graphql` (типично `/graphql` WS или отдельный path — зафиксировать в `application.yml`).
2. JWT на handshake / connection init: без валидного access token соединение не устанавливается; при logout/expiry — close.
3. Authorization на resolver subscription: только аутентифицированный user; события фильтровать sink’ом по `userId` подписчика.
4. CORS/allowed origins — как у HTTP API для локального `3001`.
5. Проверка: ws подключение с токеном ок, без токена — отказ; подписка чужого userId невозможна.

### 3.3. Публикация из домена

1. После **успешного** изменения в `UserServiceImpl` (ветки ADD/ACCEPT/REJECT/DELETE, в т.ч. cancel vs delete friend) опубликовать в `FriendshipEventPublisher` (ApplicationEvent или `Sinks.Many` per user).
2. Различать `INVITATION_CANCELLED` vs `FRIEND_DELETED` по статусу до DELETE (`PENDING` vs `ACCEPTED`/`FRIEND`).
3. Не публиковать при `FriendshipActionException`.
4. Проверка: unit/integration — после ADD оба userId получили событие нужного type; после чужого ADD третий user — тишина.

### 3.4. Фронт Apollo (`rfr-gql-client`)

1. Зависимость transport `graphql-ws` + `GraphQLWsLink`; `split`: subscription → WS, остальное → HTTP (как сейчас).
2. В connection params передавать Bearer access token; на `clearSession`/logout — dispose client/subscription.
3. Глобально (layout после входа): `useSubscription(FRIENDSHIP_EVENTS)`.
4. На событие:
   - показать snackbar по таблице §1.3 (новые строки копирайта для контрагента, напр. Invitation received / Friend added);
   - `refetchQueries` для People (`GetPeople`, `GetFriends`, `GetInvitations`, `GetOutcomeInvitations`) **или** точечный cache update; минимум — refetch как в `useUpdateFriendshipStatus` сегодня.
5. Reconnect: библиотечный retry + один refetch People при `onConnected` если route = People.
6. Проверка: два браузера, сценарии §1.6.1–6; два окна §1.6.7; logout §1.6.8.

### 3.5. Порядок внедрения

1. Схема + publisher без UI.  
2. WS security.  
3. Apollo split + подписка + snackbar.  
4. Стабилизация списков (search/pagination edge).  
5. e2e из §1.6 (отдельной задачей, не в этом плане-файле как код).

### 3.6. Предложения вне требований (не обязательны)

- Presence / typing — нет.  
- Offline queue missed events на сервере — **нет** (требования: refetch).  
- Push в ленту «новый друг» — вне рамок.  
- Dedup по event id в payload — полезно против двойного snackbar при refetch+event; **предложение**, не блокер если e2e стабильны.

---

## 4. Сверка плана с требованиями

| Требование | В техплане | Замечание |
|---|---|---|
| 5 типов событий | да, enum | ок |
| Получатели только пара + все окна юзера | да, filter by userId + multi WS | ок |
| Snackbar контрагенту в т.ч. add/remove friend | да, §3.4 | тексты Invitation received / Friend added заданы требованиями; агент не выдумал смысл |
| Обновление вкладок People | да, refetch People queries | точечный cache — опция; поведение вкладок покрыто refetch |
| Другая вкладка / не People | snackbar глобально; список при возврате | ок |
| Поиск / пагинация | слабо детализированы в шагах | **дыра:** §3.4 не расписывает «не прыгать страницу» и «не показывать вне search» — опирается на серверный search/page; нужно явно проверить руками критерии §1.3 |
| Logout / 401 | да | ок |
| Reconnect без missed push + refetch | да | ок |
| SLA 3с | нет отдельного шага измерения | **дыра:** добавить в проверку e2e timeout/assert, не в архитектуру |
| Вне рамок лента/фото | соблюдено | ок |
| Критерии §1.6 | перечислены как проверки | ок; код тестов не пишем |

**Что агент мог бы выдумать, но помечено / не сделано обязательным:** серверная очередь missed events; event id dedup; отдельный WS path — допустимо как техника, на продукт не влияет.

**Критерии, которые шагами техплана проверяются плохо без e2e:** пагинация «строка на другой странице не прыгает»; поиск; SLA 3с — закрываются только сценариями §1.6 и доп. ручными кейсами, не unit’ом publisher.

**Итог сверки:** продукт из §1 техпланом покрыт; добить в реализации чеклист search/pagination/SLA. Расхождений «агент придумал обязательное поведение против требований» нет; осознанный продуктовый выбор требований — snackbar у контрагента и refetch вместо durable miss-queue.
