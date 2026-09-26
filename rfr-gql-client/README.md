# rfr-gql-client - Frontend приложение Rangiffler

## Описание

`rfr-gql-client` - клиентское React приложение проекта Rangiffler для работы с фотографиями из путешествий. Приложение использует GraphQL для взаимодействия с бэкендом и OAuth 2.0 PKCE flow для аутентификации.

## Технологический стек

- **React 18** - UI библиотека
- **TypeScript 5.2** - типизация
- **Vite 5** - сборщик и dev-сервер
- **Apollo Client 3.9** - GraphQL клиент
- **Material-UI 5** - UI компоненты
- **React Router 6** - маршрутизация
- **crypto-js** - криптография для PKCE
- **react-svg-worldmap** - визуализация карты мира

## Архитектура приложения

### Структура проекта

```
rfr-gql-client/
├── src/
│   ├── api/                      # API клиенты и утилиты
│   │   ├── apolloClient.ts       # Apollo Client конфигурация
│   │   ├── authClient.ts         # HTTP клиент для OAuth
│   │   └── authUtils.ts          # PKCE утилиты (code verifier, challenge)
│   ├── components/               # React компоненты
│   │   ├── AppContent/           # Корневой контент с роутингом
│   │   ├── Drawer/               # Боковое меню
│   │   ├── MenuAppBar/           # Верхняя панель навигации
│   │   ├── PrivateRoute/         # HOC для защищенных маршрутов
│   │   ├── PhotoCard/            # Карточка фотографии
│   │   ├── PhotoContainer/       # Контейнер для списка фото
│   │   ├── PhotoModal/           # Модальное окно добавления фото
│   │   ├── PhotoPagination/      # Пагинация фотографий
│   │   ├── ProfileForm/          # Форма редактирования профиля
│   │   ├── PeopleTable/          # Таблицы пользователей
│   │   │   ├── AllTable/         # Все пользователи
│   │   │   ├── FriendsTable/     # Друзья
│   │   │   ├── InvitationsTable/ # Входящие заявки
│   │   │   └── OutcomeInvitationsTable/ # Исходящие заявки
│   │   ├── Table/                # Переиспользуемые компоненты таблиц
│   │   │   ├── ActionButtons/    # Кнопки действий (Add, Remove, Accept, Decline)
│   │   │   ├── HeadCell/         # Ячейка заголовка
│   │   │   ├── TableHead/        # Заголовок таблицы
│   │   │   ├── TableToolbar/     # Панель инструментов (поиск)
│   │   │   └── Pagination/       # Пагинация таблицы
│   │   ├── CountrySelect/        # Выбор страны
│   │   ├── ImageUpload/          # Загрузка изображений
│   │   ├── WorldMap/             # Карта мира со статистикой
│   │   ├── Loader/               # Индикатор загрузки
│   │   ├── Sidebar/              # Боковая панель
│   │   ├── TabPanel/             # Панель вкладок
│   │   └── Toggle/               # Переключатель
│   ├── context/                  # React Context
│   │   ├── SessionContext.tsx    # Контекст сессии пользователя
│   │   ├── CountriesContext.tsx  # Контекст списка стран
│   │   ├── DialogContext.tsx     # Контекст модальных окон
│   │   └── SnackBarContext.tsx   # Контекст уведомлений
│   ├── hooks/                    # Custom React hooks
│   │   ├── useGetFeed.ts         # Получение ленты фотографий
│   │   ├── useGetUser.ts         # Получение данных пользователя
│   │   ├── useGetCountries.ts    # Получение списка стран
│   │   ├── useGetFriends.ts      # Получение друзей
│   │   ├── useGetInvitations.ts  # Получение входящих заявок
│   │   ├── useGetOutcomeInvitations.ts # Получение исходящих заявок
│   │   ├── useQueryPeople.ts     # Поиск пользователей
│   │   ├── useCreatePhoto.ts     # Создание фотографии
│   │   ├── useUpdatePhoto.ts     # Обновление фотографии
│   │   ├── useDeletePhoto.ts     # Удаление фотографии
│   │   ├── useLikePhoto.ts       # Лайк фотографии
│   │   ├── useUpdateUser.ts      # Обновление профиля
│   │   └── useUpdateFriendshipStatus.ts # Управление дружбой
│   ├── pages/                    # Страницы приложения
│   │   ├── LandingPage/          # Главная страница (не авторизован)
│   │   ├── Authorized/           # Обработка OAuth callback
│   │   ├── Redirect/             # Редирект после логина
│   │   ├── Logout/               # Страница выхода
│   │   ├── MyTravelsPage/        # Мои путешествия (лента фото)
│   │   ├── PeoplePage/           # Люди (друзья, заявки)
│   │   └── ProfilePage/          # Профиль пользователя
│   ├── types/                    # TypeScript типы
│   │   ├── User.ts               # Тип пользователя
│   │   ├── Photo.ts              # Тип фотографии
│   │   ├── Country.ts            # Тип страны
│   │   ├── Likes.ts              # Тип лайков
│   │   └── Order.ts              # Тип сортировки
│   ├── utils/                    # Утилиты
│   │   ├── arrays.ts             # Работа с массивами
│   │   └── comparator.ts         # Функции сравнения для сортировки
│   ├── App.tsx                   # Корневой компонент
│   ├── main.tsx                  # Точка входа
│   ├── theme.tsx                 # Material-UI тема
│   └── index.css                 # Глобальные стили
├── public/                       # Статические файлы
├── .env                          # Переменные окружения
├── vite.config.ts                # Конфигурация Vite
├── tsconfig.json                 # Конфигурация TypeScript
└── package.json                  # Зависимости и скрипты
```

## Ключевые архитектурные решения

### 1. GraphQL с Apollo Client

Все запросы к бэкенду выполняются через GraphQL:

```typescript
// apolloClient.ts
const apolloHttpLink = createHttpLink({
  uri: `${API_URL}/graphql`,
})

const headerLink = setContext((_request, previousContext) => ({
  headers: {
    ...previousContext.headers,
    "Authorization": idTokenFromLocalStorage() ? `Bearer ${idTokenFromLocalStorage()}` : "",
  },
}));

export const apiClient = new ApolloClient({
  link: headerLink.concat(apolloHttpLink),
  cache: new InMemoryCache(),
});
```

### 2. OAuth 2.0 PKCE Flow

Реализован полный PKCE flow для безопасной аутентификации:

**Генерация Code Verifier и Challenge:**
```typescript
// authUtils.ts
const generateCodeVerifier = () => {
  return base64Url(crypto.enc.Base64.stringify(crypto.lib.WordArray.random(32)));
}

const generateCodeChallenge = () => {
  const codeVerifier = localStorage.getItem("codeVerifier");
  return base64Url(sha256(codeVerifier!));
}
```

**Процесс аутентификации:**
1. Генерация `code_verifier` и `code_challenge`
2. Редирект на Authorization Server с `code_challenge`
3. Получение `authorization_code` после успешной аутентификации
4. Обмен `code` + `code_verifier` на токены
5. Сохранение `id_token` в localStorage

### 3. Custom Hooks для GraphQL

Каждый GraphQL запрос инкапсулирован в custom hook:

```typescript
// useGetFeed.ts
export const useGetFeed = (req: getFeedRequestType) => {
  const {data, loading, error, refetch, fetchMore} = useQuery(GET_FEED, {
    variables: {
      withFriends: req.withFriends,
      page: req.page ?? 0,
      size: 12,
    },
    fetchPolicy: "cache-and-network",
  });
  
  return {
    photos: data?.feed?.photos?.edges?.map((e: any) => e?.node) ?? [],
    stat: data?.feed?.stat,
    hasPreviousPage: data?.feed?.photos?.pageInfo?.hasPreviousPage,
    hasNextPage: data?.feed?.photos?.pageInfo?.hasNextPage,
    loading,
    error,
    refetch,
    fetchMore,
  };
}
```

### 4. Context API для глобального состояния

**SessionContext** - данные текущего пользователя:
```typescript
interface SessionContextInterface {
  updateUser: () => void;
  user?: User;
}
```

**CountriesContext** - список стран для выбора:
```typescript
interface CountriesContextInterface {
  countries: Country[];
  loading: boolean;
}
```

**DialogContext** - управление модальными окнами

**SnackBarContext** - уведомления пользователю

### 5. Защищенные маршруты

`PrivateRoute` компонент проверяет аутентификацию через GraphQL запрос `useGetUser()`:

```typescript
export const PrivateRoute = () => {
  const {data, loading, refetch} = useGetUser();
  const sessionContext = {user: data?.user, updateUser: refetch};

  return (
    loading ? <Loader/> :
    data ? (
      <SessionContext.Provider value={sessionContext}>
        <CountriesProvider>
          <DialogProvider>
            <MenuAppBar />
            <Outlet/>
          </DialogProvider>
        </CountriesProvider>
      </SessionContext.Provider>
    ) : <Navigate to="/" replace={true}/>
  )
}
```

### 6. Компонентная архитектура

**Атомарные компоненты:**
- `Loader`, `Toggle`, `ImageUpload` - переиспользуемые UI элементы

**Композитные компоненты:**
- `PhotoCard` - карточка фото с лайками и действиями
- `PeopleTable` - таблица с пользователями, пагинацией и поиском

**Контейнеры:**
- `PhotoContainer` - управление списком фотографий
- `AppContent` - роутинг и layout

## Основные функции

### Аутентификация
- OAuth 2.0 Authorization Code Flow с PKCE
- Автоматическое создание пользователя при первом входе
- Logout с редиректом на Authorization Server

### Работа с фотографиями
- Просмотр ленты (свои + друзей)
- Загрузка новых фотографий с выбором страны
- Редактирование описания
- Удаление фотографий
- Лайки фотографий
- Пагинация

### Социальные функции
- Поиск пользователей
- Отправка заявок в друзья
- Принятие/отклонение заявок
- Удаление из друзей
- Просмотр статусов дружбы (NOT_FRIEND, FRIEND, INVITATION_SENT, INVITATION_RECEIVED)

### Профиль
- Редактирование имени и фамилии
- Загрузка аватара
- Выбор страны проживания
- Просмотр статистики по странам

### Визуализация
- Карта мира с количеством фото по странам
- Статистика путешествий

## Конфигурация

### Переменные окружения (.env)

```env
VITE_AUTH_URL=http://localhost:9001      # URL Authorization Server
VITE_API_URL=http://localhost:8081       # URL GraphQL API
VITE_FRONT_HOST=localhost                # Хост фронтенда
VITE_FRONT_URL=http://localhost:3001     # URL фронтенда
VITE_CLIENT_ID=client                    # OAuth Client ID
```

### Vite конфигурация

```typescript
export default defineConfig(({mode}) => {
  process.env = {...process.env, ...loadEnv(mode, process.cwd())};

  return defineConfig({
    plugins: [react()],
    server: {
      host: process.env.VITE_FRONT_HOST,
      port: 3001,
    },
  });
});
```

## Запуск

### Требования
- Node.js 18+
- npm или yarn

### Установка зависимостей
```bash
npm install
```

### Запуск dev-сервера
```bash
npm run dev
```

Приложение будет доступно по адресу: http://localhost:3001

### Сборка для production
```bash
npm run build
```

### Линтинг
```bash
npm run lint
```

## Маршруты приложения

- `/` - Landing page (не авторизован)
- `/authorized` - OAuth callback для обработки authorization code
- `/redirect` - Промежуточный редирект после логина
- `/logout` - Страница выхода
- `/my-travels` - Лента фотографий (защищенный маршрут)
- `/people` - Управление друзьями (защищенный маршрут)
- `/profile` - Профиль пользователя (защищенный маршрут)

## GraphQL запросы

### Queries

**feed** - получение ленты фотографий:
```graphql
query GetFeed($withFriends: Boolean!) {
  feed(withFriends: $withFriends) {
    photos(page: 0, size: 12) {
      edges { node { id, src, country, description, likes } }
      pageInfo { hasPreviousPage, hasNextPage }
    }
    stat { count, country { code } }
  }
}
```

**user** - получение данных пользователя:
```graphql
query GetUser {
  user {
    id, username, firstname, surname, avatar, location
  }
}
```

**countries** - список стран:
```graphql
query GetCountries {
  countries { code, name, flag }
}
```

### Mutations

**photo** - создание фотографии:
```graphql
mutation CreatePhoto($input: PhotoInput!) {
  photo(input: $input) {
    id, src, country, description
  }
}
```

**updateUser** - обновление профиля:
```graphql
mutation UpdateUser($input: UserInput!) {
  updateUser(input: $input) {
    id, username, firstname, surname, avatar
  }
}
```

**updateFriendship** - управление дружбой:
```graphql
mutation UpdateFriendship($input: FriendshipInput!) {
  updateFriendship(input: $input) {
    id, username, friendStatus
  }
}
```