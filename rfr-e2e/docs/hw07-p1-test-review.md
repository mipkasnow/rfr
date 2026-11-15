# test-reviewer по пункту 1

## Промпт
Ревью пункта 1 ДЗ 07: владение стабами двух контекстов, GqlStubsIsolationTest,
GqlStubsExtensionTest, docs красных проверок, SessionExtension/фикстуры/GqlStubs.
Код не править. Формат skill rfr-test-review.

## Ответ агента (сжато)
Дефектов владения стабами нет.
- Основной GqlStubs — Store GqlStubsExtension, close через Store на открытом контексте.
- Сессионный — фикстура при useStubs=true, TearDown: stubs.close → context.close.
- Изоляция: разные BrowserContext, route/unroute только на своём.
- Пробы GqlStubsExtensionTest зелёные; порядок close stubs→context доказан падением пробы при перестановке.

## Исправления по замечаниям
Не требовались: подтверждённых дефектов не было.
