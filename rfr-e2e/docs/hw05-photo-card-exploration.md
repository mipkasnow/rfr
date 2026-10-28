# Пункт 2: исследование My Travels (Playwright MCP)

## Промпт

Исследуй ленту My Travels на 1920×1080 и 390×844 через Playwright MCP после логина.
Предложи `PhotoCard` и локаторы страницы с доказательством из accessibility snapshot /
`count()`, что при семи карточках (With friends) каждый локатор однозначен.
Проверь грабли: like от page, страна в tooltip, чип фильтра, счётчик countries visited.

## Ответ агента (сжато)

- Фронт: `role="article"` + `aria-labelledby` из `useId()` для страны и описания.
- Карточка: `getByRole(ARTICLE).filter(hasText(описание))` — на 7 карточках `count()==1` для каждого описания (1920 и 390).
- Внутри карточки: `getByRole(BUTTON, "like"|"Photo actions", exact)` — `count()==1`; у чужих фото `Photo actions == 0`.
- Like от `page`: strict mode, 7 элементов — см. `hw05-strict-mode-like.txt`.
- Страна: `getByRole(TOOLTIP).getByRole(BUTTON, name, exact)`; с page exact — 2, без exact — 3.
- Чип: `Country filter: <страна>`; сброс: `Reset country filter`.
- Счётчик: `getByRole(BUTTON, "countries visited")` только при закрытом списке; проверка через `hasText`, не через имя.

## Что поправлено студентом в ответе агента

- (заполнить при ревью перед сдачей PR)
