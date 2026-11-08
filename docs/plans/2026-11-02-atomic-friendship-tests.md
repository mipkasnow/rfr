# План: атомарные тесты дружбы на двух сессиях

Ветка `v3/l06-pw-contexts-live`. Дата: 2026-11-02.

## Context

`FriendshipFlowTest.friendshipIsCreatedAndRemoved` проверяет весь путь дружбы одним сценарием: Add → Accept → Friends у обоих → Remove → пусто у обоих. Падение на любом шаге скрывает остальные, а промежуточные `reload()` нужны только потому, что обе стороны держат открытые страницы People. Нужны пять атомарных тестов — отправка заявки, Accept, Decline, удаление со стороны отправителя дружбы и со стороны получателя — каждый с одним действием в одном контексте и одной итоговой проверкой в другом. Большой сценарий удаляется.

## Подход

Каждый тест — два участника: **актор** выполняет одно действие, **наблюдатель** в другом `BrowserContext` видит результат. Предусловие создаёт фикстура, а не UI:

- `@Session @User(outcomeInvitations = 1) UserSession alice` — Alice уже отправила заявку (`UserWithOutcomeInvitationsFixture`: `Friendship(alice, invitee, PENDING)`); приглашённый `alice.user().outcomeInvitations().getFirst()` — `TestUser` с паролем, он входит в основной `Page` через `PeopleLogin.open`.
- `@Session @User(friends = 1) UserSession alice` — Alice и друг уже друзья (`UserWithFriendsFixture`: `Friendship(alice, friend, ACCEPTED)`, Alice — requester); друг входит в основной `Page`.
- Отправка заявки: `@User TestUser alice` в основном `Page` и `@Session @User UserSession bob` без связей.

Синхронизация и порядок, без `reload()`:
1. Актор нажимает кнопку и ждёт snackbar (`PeoplePage.checkThatNotification(Notification.…)` → `Snackbar`) — правило 15 skill `rfr-page-objects`, уведомление появляется только после ответа API.
2. Только после этого наблюдатель впервые открывает People (`clickPeople()` из `MainPage` сессии или `PeopleLogin.open` в основном `Page`) — запрос свежий, устаревшего кэша вкладки нет.
3. Последний шаг теста — единственная итоговая проверка наблюдателя через `PeopleTable.checkThatPeople` или `checkThatEmpty`.

## Тесты

Класс `test/people/FriendshipFlowTest.java` переименовывается `git mv` в `test/people/FriendshipActionsTest.java` (`@UiTest`, `@DisplayName("Действия с дружбой на двух сессиях")`); метод `friendshipIsCreatedAndRemoved` удаляется.

| Тест | Предусловие | Актор и действие | Синхронизация | Итоговая проверка наблюдателя |
|---|---|---|---|---|
| `invitationReachesAddressee` | `@User TestUser alice`, `@Session @User UserSession bob` | Alice в `Page`: All People → `searchPeople(bob)` → `clickAdd()` | `INVITATION_SENT` | Bob: People → Income invitations → `checkThatPeople([alice])` |
| `acceptedInvitationMakesFriends` | `@Session @User(outcomeInvitations = 1) UserSession alice` | приглашённый в `Page`: Income invitations → `row(alice).clickAccept()` | `INVITATION_ACCEPTED` | Alice: People → Friends → `checkThatPeople([invitee])` |
| `declinedInvitationLeavesSender` | то же | приглашённый: Income invitations → `row(alice).clickDecline()` | `INVITATION_DECLINED` | Alice: People → Outcome invitations → `checkThatEmpty()` |
| `friendRemovedByRequesterDisappearsForAddressee` | `@Session @User(friends = 1) UserSession alice` | Alice в сессии: People → Friends → `row(friend).clickRemove()` | `FRIEND_DELETED` | друг в `Page`: `PeopleLogin.open` → Friends → `checkThatEmpty()` |
| `friendRemovedByAddresseeDisappearsForRequester` | то же | друг в `Page`: `PeopleLogin.open` → Friends → `row(alice).clickRemove()` | `FRIEND_DELETED` | Alice в сессии: People → Friends → `checkThatEmpty()` |

В двух тестах удаления наблюдатель основного `Page` входит уже после действия, поэтому его People тоже открывается впервые.

## Изменения в Page Objects и enum

- `ui/component/PeopleRow`: `clickDecline()` — `button(PeopleAction.DECLINE).click()`, как `clickAccept`.
- `Notification.INVITATION_SENT` и `INVITATION_DECLINED` начинают использоваться; новых значений не нужно.
- `ui/page/PeoplePage.reload()` после удаления большого сценария никем не вызывается — удалить (YAGNI); `ProfilePage.reload()` не трогаем.
- Остальное переиспользуется без изменений: `PeopleLogin.open`, `MainPage.clickPeople`, `PeoplePage.openTab/searchPeople/table/checkThatNotification`, `PeopleTable.row/checkThatPeople/checkThatEmpty`, `PeopleRow.clickAdd/clickAccept/clickRemove`, `PeopleTab`, `PeopleAction`, `Notification`, `Snackbar`.

Extensions и фикстуры не меняются: `UserExtension` создаёт обоих участников и их связи, `SessionExtension` открывает контекст сессии и входит через UI, оба удаляют своё после теста (правила 2, 5, 6, 11, 14 skill `rfr-test-review`).

## Шаги

1. `git mv` `FriendshipFlowTest.java` → `FriendshipActionsTest.java`, заменить метод большого сценария тестом `invitationReachesAddressee`; три прогона — **сделано**: три прогона зелёные, с неверным ожиданием тест падает.
   **Находка и исправление фронта.** `PeopleTable` показывал «There are no users yet» при `!data?.length`, то есть и пока данные грузятся, поэтому `PeopleTable.checkThatEmpty()` проходил на непустой вкладке. Исправлено в `rfr-gql-client`: `PeopleTable` получает `loading` из хуков вкладок, на первой загрузке показывает `CircularProgress` с `aria-label="Loading users"`, а пустое состояние — только после загрузки. После исправления `checkThatEmpty()` на непустой вкладке падает, все тесты People зелёные; Page Objects не менялись.
2. `PeopleRow.clickDecline`; тесты Accept и Decline; по три прогона — **сделано**: `acceptedInvitationMakesFriends` и `declinedInvitationLeavesSender` зелёные по три раза; с обратным ожиданием (`checkThatEmpty` для Accept, `checkThatPeople([invitee])` для Decline) оба падают.
3. Два теста удаления; по три прогона — **сделано**: `friendRemovedByRequesterDisappearsForAddressee` и `friendRemovedByAddresseeDisappearsForRequester` зелёные по три раза; с ожиданием второго участника в Friends вместо пустой вкладки оба падают. Удаление со стороны addressee снимает дружбу и у requester — риск направленного хранения не подтвердился.
4. Удалить `PeoplePage.reload()`; компиляция и прогон всех тестов People — **сделано**: тесты People 17 из 17; `FriendshipActionsTest` в узкой раскладке 390×844 — три прогона по 5 из 5; `./gradlew e2e` — 32 теста, 0 падений, 0 пропусков.
5. README «Архитектура» не меняется (тесты лежат в `test/people`); в `docs/plans/2026-11-02-second-browser-session.md` шаг 6 дополнить ссылкой на этот план — **сделано**.

## Проверка

- Каждый новый тест — не меньше трёх прогонов подряд, отдельно:
  `for i in 1 2 3; do ./gradlew :rfr-e2e:e2e --tests '*FriendshipActionsTest.<метод>'; done`
  и по `TEST-*.xml`: `tests=1 failures=0 errors=0` в каждом прогоне.
- Узкая раскладка People — те же три прогона класса целиком с `-Drfr.viewportWidth=390 -Drfr.viewportHeight=844` (контекст сессии получает те же настройки).
- Итоговый `./gradlew e2e` — весь набор зелёный, пропущенных нет.
- Красный тест для каждой итоговой проверки: временно подменить ожидание (например, `checkThatEmpty` вместо `checkThatPeople`) и увидеть падение по таймауту web-first assertion, затем вернуть — подтверждает, что проверка наблюдателя не тавтологична.

## Риски

- Если backend хранит дружбу направленно и удаление addressee не снимает строку requester, тест `friendRemovedByAddresseeDisappearsForRequester` упадёт — это находка о продукте, а не о тесте; разбирать по trace.
- Snackbar предыдущего действия в том же контексте мог бы дать ложную синхронизацию; в каждом тесте у контекста актора одно действие, поэтому уведомление одно.
