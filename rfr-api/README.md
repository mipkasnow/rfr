# RFR-API - GraphQL Backend

## Описание

`rfr-api` - монолитный бэкенд проекта Rangiffler с GraphQL API, реализующий OAuth 2.0 Resource Server. Модуль предоставляет GraphQL API для работы с фотографиями, пользователями и странами.
Поддерживает как реальную работу с базой данных, так и mock-контроллеры для тестирования.

## Архитектура

### Технологический стек

- **Spring Boot 3.x**
- **Spring for GraphQL** - GraphQL API
- **Spring Security 7.x** - OAuth 2.0 Resource Server
- **Spring Data JPA** - работа с базой данных
- **Flyway** - миграции БД
- **MySQL 8** - СУБД

### Структура модуля

```
rfr-api/
├── src/main/java/io/student/rangiffler/
│   ├── RangifflerApiApplication.java           # Точка входа
│   ├── config/
│   │   └── RococoApiConfiguration.java         # Security конфигурация
│   ├── controller/
│   │   ├── FeedMockQueryController.java        # GraphQL Query контроллер (Mock)
│   │   ├── PhotoMockMutationController.java    # GraphQL Mutation контроллер (Mock)
│   │   ├── CountryQueryController.java         # GraphQL Query контроллер стран
│   │   ├── UserQueryController.java            # GraphQL Query контроллер пользователей
│   │   └── UserMutationController.java         # GraphQL Mutation контроллер пользователей
│   ├── data/
│   │   ├── entity/
│   │   │   ├── CountryEntity.java              # Entity страны
│   │   │   ├── UserEntity.java                 # Entity пользователя
│   │   │   ├── FriendshipEntity.java           # Entity дружбы
│   │   │   ├── FriendShipId.java               # Composite key для FriendshipEntity
│   │   │   └── FriendshipStatus.java           # Enum статуса дружбы
│   │   ├── projection/
│   │   │   └── UserWithStatus.java             # Projection для пользователя со статусом
│   │   └── repository/
│   │       ├── CountryRepository.java          # JPA репозиторий стран
│   │       └── UserRepository.java             # JPA репозиторий пользователей
│   ├── service/
│   │   ├── api/
│   │   │   ├── CountryService.java             # Интерфейс сервиса стран
│   │   │   └── UserService.java                # Интерфейс сервиса пользователей
│   │   ├── impl/
│   │   │   ├── CountryServiceImpl.java         # Реализация сервиса стран
│   │   │   └── UserServiceImpl.java            # Реализация сервиса пользователей
│   │   └── cors/
│   │       └── CorsCustomizer.java             # CORS настройки
│   ├── util/
│   │   ├── BytesAsString.java                  # Конвертер byte[] -> String
│   │   ├── StringAsBytes.java                  # Конвертер String -> byte[]
│   │   └── GqlQueryPaginationAndSort.java      # Утилита для пагинации GraphQL
│   └── exception/
│       └── ResourceNotFoundException.java      # Custom exception
└── src/main/resources/
    ├── application.yml                          # Конфигурация приложения
    ├── graphql/
    │   └── schema.graphqls                      # GraphQL схема
    ├── db/migration/                            # Flyway миграции
    └── mock/                                    # Mock данные (JSON)
        ├── query_feed.json                      # Mock данные для feed без друзей
        ├── query_feed_with_friends.json         # Mock данные для feed с друзьями
        └── mutation_like.json                   # Mock данные для лайков
```

## GraphQL API

### Схема

GraphQL схема определена в `src/main/resources/graphql/schema.graphqls`

### Контроллеры

#### Query контроллеры

- **FeedMockQueryController** - возвращает mock данные для ленты фотографий
  - `@QueryMapping` для `feed` query
  - `@SchemaMapping` для резолверов полей `Feed.stat`, `Feed.photos`, `Photo.likes`
  - Читает данные из JSON файлов в `resources/mock/`

- **CountryQueryController** - работа со странами
  - Использует `CountryService` для доступа к БД

- **UserQueryController** - работа с пользователями
  - Использует `UserService` для доступа к БД

#### Mutation контроллеры

- **PhotoMockMutationController** - mock мутации для фотографий
  - Создание и удаление фотографий (mock)
  - Лайки фотографий (mock)

- **UserMutationController** - мутации пользователей
  - Обновление профиля пользователя
  - Управление друзьями

### Mock контроллеры

Mock контроллеры читают данные напрямую из JSON файлов без обращения к сервисам. Это временное решение для тестирования фронтенда.

**Особенности:**
- Используют `ObjectMapper` для парсинга JSON
- Читают файлы через `ClassPathResource`
- Возвращают данные напрямую из контроллера
- Выбор файла зависит от параметров запроса (например, `withFriends`)

## Security конфигурация

### OAuth 2.0 Resource Server

Все GraphQL endpoints защищены через JWT токены:

```java
@Controller
@PreAuthorize("isAuthenticated()")
public class FeedMockQueryController {
  // ...
}
```

### JWT Token валидация

1. Проверка подписи токена через JWKS endpoint Authorization Server
2. Валидация `iss` (issuer) - должен совпадать с `issuer-uri`
3. Валидация `exp` (expiration) - токен не должен быть просрочен
4. Извлечение `sub` (subject) - username пользователя через `@AuthenticationPrincipal Jwt`

## GraphQL Queries и Mutations

### Queries

#### feed

Получение ленты фотографий пользователя

```graphql
query GetFeed($withFriends: Boolean!) {
  feed(withFriends: $withFriends) {
    stat {
      count
      country {
        code
        name
        flag
      }
    }
    photos(page: 0, size: 10) {
      content {
        id
        src
        description
        country {
          code
          name
          flag
        }
        likes {
          total
          likes {
            user
          }
        }
      }
      totalElements
    }
  }
}
```

**Параметры:**
- `withFriends: Boolean!` - включать ли фотографии друзей

**Mock данные:**
- `withFriends: false` → `query_feed.json`
- `withFriends: true` → `query_feed_with_friends.json`

#### countries

Получение списка стран

```graphql
query GetCountries {
  countries {
    code
    name
    flag
  }
}
```

#### user

Получение информации о пользователе

```graphql
query GetUser {
  user {
    id
    username
    firstname
    lastname
    avatar
    friends {
      id
      username
    }
  }
}
```

### Mutations

#### photo (создание)

Создание новой фотографии (mock)

```graphql
mutation CreatePhoto($input: PhotoInput!) {
  photo(input: $input) {
    id
    src
    country {
      code
      name
    }
    description
  }
}
```

**Input:**
```json
{
  "input": {
    "src": "data:image/jpeg;base64,...",
    "countryCode": "ru",
    "description": "Описание фотографии"
  }
}
```

#### deletePhoto

Удаление фотографии (mock)

```graphql
mutation DeletePhoto($id: ID!) {
  deletePhoto(id: $id)
}
```

#### updateUser

Обновление профиля пользователя

```graphql
mutation UpdateUser($input: UserInput!) {
  updateUser(input: $input) {
    id
    username
    firstname
    lastname
    avatar
  }
}
```

## Mock данные

### Структура JSON файлов

Mock данные хранятся в `src/main/resources/mock/` в упрощенном формате:

**query_feed.json** - лента без друзей:
```json
{
  "photos": {
    "edges": [
      {
        "node": {
          "id": "uuid",
          "src": "",
          "country": {
            "code": "ru",
            "name": "Russian Federation",
            "flag": ""
          },
          "description": "Описание",
          "likes": {
            "total": 0,
            "likes": []
          }
        }
      }
    ],
    "pageInfo": {
      "hasPreviousPage": false,
      "hasNextPage": false
    }
  },
  "stat": [
    {
      "count": 1,
      "country": {
        "code": "ru"
      }
    }
  ]
}
```

**query_feed_with_friends.json** - лента с друзьями (аналогичная структура, больше фотографий)

**mutation_like.json** - данные для лайков (используется в PhotoMockMutationController)

### Загрузка mock данных

Mock контроллеры используют метод `loadMockData(String filename)` для чтения JSON:

```java
private JsonNode loadMockData(String filename) {
  try {
    ClassPathResource resource = new ClassPathResource("mock/" + filename);
    try (InputStream inputStream = resource.getInputStream()) {
      return objectMapper.readTree(inputStream);
    }
  } catch (IOException e) {
    throw new RuntimeException("Failed to load mock data from " + filename, e);
  }
}
```

## Сервисный слой

### CountryService

**Методы:**

- `List<Country> allCountries()` - получение всех стран
- `Country findByCode(String code)` - поиск по коду

**Особенности:**

- `@Transactional(readOnly = true)` для read операций
- Использование `Optional.orElseThrow()` с `NotFoundException`

### UserService

**Методы:**

- `User currentUser(String username)` - получение текущего пользователя
- `User updateUser(UserInput input, String username)` - обновление профиля
- `List<Stat> stat(String username, boolean withFriends)` - статистика по странам

**Особенности:**

- Автоматическое создание пользователя при первой аутентификации
- Обработка аватаров через конвертеры

## Обработка ошибок

GraphQL обрабатывает ошибки через стандартный механизм Spring for GraphQL:

```json
{
  "errors": [
    {
      "message": "Photo not found",
      "locations": [{"line": 2, "column": 3}],
      "path": ["feed", "photos", 0],
      "extensions": {
        "classification": "NOT_FOUND"
      }
    }
  ],
  "data": null
}
```

**Обрабатываемые исключения:**

1. **NotFoundException** - ресурс не найден
2. **IllegalArgumentException** - некорректные аргументы
3. **IllegalStateException** - операция невозможна
4. **RuntimeException** - общие ошибки выполнения

## База данных

### Flyway миграции

Расположение: `src/main/resources/db/migration/`

### Основные таблицы

- **user** - пользователи
- **country** - страны
- **friendship** - связи между пользователями (друзья)

## Запуск

### Требования

- Java 21
- Docker
- Gradle 9.2.1

### Локальный запуск

```bash
# 1. Запустить PostgreSQL и другие зависимости
bash localenv.sh

# 2. Запустить rfr-api
cd rfr-api
../gradlew bootRun

# Или через IDE
# Main class: io.student.rangiffler.RangifflerApiApplication
```

### Проверка работоспособности

GraphQL endpoint доступен по адресу: `http://localhost:8081/graphql`

GraphiQL интерфейс: `http://localhost:8081/graphiql`

**Пример запроса:**

```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "query { feed(withFriends: false) { stat { count country { code name } } } }"
  }'
```
