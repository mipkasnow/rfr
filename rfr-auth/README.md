# RFR-AUTH - Authorization Server

## Описание

`rfr-auth` - модуль авторизации и аутентификации для проекта Rococo, реализующий OAuth 2.0 Authorization Server на базе
Spring Authorization Server. Модуль отвечает за регистрацию пользователей, аутентификацию и выдачу JWT токенов.

## Архитектура

### Технологический стек

- **Spring Boot 4.0.2**
- **Spring Security 7.x**
- **Spring Authorization Server** - OAuth 2.0 / OpenID Connect 1.0
- **Spring Data JPA** - работа с базой данных
- **Thymeleaf** - шаблонизатор для UI
- **Flyway** - миграции БД
- **MySQL 8** - СУБД

### Структура модуля

```
rfr-auth/
├── src/main/java/io/student/rfr/
│   ├── RangifflerAuthApplication.java          # Точка входа
│   ├── config/
│   │   └── RangifflerAuthServiceConfig.java    # Конфигурация Security и OAuth2
│   ├── controller/
│   │   ├── LoginController.java            # Обработка логина
│   │   ├── RegisterController.java         # Регистрация пользователей
│   │   └── CustomErrorController.java      # Обработка ошибок
│   ├── data/
│   │   ├── Authority.java                  # Enum ролей (read, write)
│   │   ├── AuthorityEntity.java            # Entity для authorities
│   │   ├── UserEntity.java                 # Entity пользователя
│   │   └── repository/
│   │       └── UserRepository.java         # JPA репозиторий
│   ├── model/
│   │   └── RegistrationForm.java           # DTO для регистрации
│   ├── service/
│   │   ├── api/
│   │   │   └── UserService.java            # Интерфейс сервиса
│   │   ├── impl/
│   │   │   ├── UserServiceImpl.java        # Регистрация пользователей
│   │   │   └── DatabaseUserDetailsService.java # Загрузка UserDetails
│   │   ├── GlobalExceptionHandler.java     # Глобальная обработка ошибок
│   │   ├── OidcClearCookiesLogoutHandler.java # Logout handler
│   │   └── cors/
│   │       └── CorsCustomizer.java         # CORS настройки
│   └── validation/
│       ├── EqualPasswords.java             # Валидация совпадения паролей
│       ├── EqualPasswordsValidator.java
│       ├── NoWhitespace.java               # Валидация отсутствия пробелов
│       └── NoWhitespaceValidator.java
└── src/main/resources/
    ├── application.yaml                     # Конфигурация приложения
    ├── db/migration/                        # Flyway миграции
    ├── static/                              # Статические ресурсы (CSS, JS, images)
    └── templates/                           # Thymeleaf шаблоны (login, register, error)
```

## Security конфигурация

### OAuth 2.0 / OpenID Connect

**RangifflerAuthServiceConfig** настраивает два Security Filter Chain:

#### 1. Authorization Server Security Filter Chain (Order 1)

```java

@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
```

**Функции:**

- Обработка OAuth 2.0 endpoints (`/oauth2/authorize`, `/oauth2/token`, etc)
- OIDC endpoints (`/.well-known/openid-configuration`, `/userinfo`)
- Logout endpoint с очисткой cookies
- Редирект на `/login` для неаутентифицированных запросов

**Endpoints:**

- `GET /oauth2/authorize` - Authorization endpoint
- `POST /oauth2/token` - Token endpoint
- `POST /oauth2/revoke` - Token revocation
- `POST /oauth2/introspect` - Token introspection
- `GET /.well-known/openid-configuration` - OpenID configuration
- `GET /.well-known/jwks.json` - JSON Web Key Set
- `POST /connect/logout` - OIDC logout

#### 2. Default Security Filter Chain (Order 2)

```java

@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
```

**Публичные endpoints:**

- `/.well-known/**` - OpenID Discovery
- `/register` - Регистрация
- `/error` - Страница ошибок
- `/images/**`, `/styles/**`, `/scripts/**`, `/fonts/**` - Статика

**Защищенные endpoints:**

- `/login` - Страница логина (form-based authentication)
- `/` - Главная страница (редирект на frontend)

### Password Encoding

```java

@Bean
public PasswordEncoder passwordEncoder() {
  return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Использует `{bcrypt}` по умолчанию

## Основные процессы

### 1. Регистрация пользователя

**Endpoint:** `POST /register`

**Процесс:**

1. Валидация формы (`@Valid RegistrationForm`)
    - Username: не пустой, без пробелов
    - Password: не пустой, без пробелов
    - PasswordSubmit: совпадает с password
2. Создание `UserEntity` с bcrypt паролем
3. Добавление authorities: `read`, `write`
4. Сохранение в БД через `UserRepository`
5. Обработка `DataIntegrityViolationException` при дубликате username

### 2. Аутентификация OAuth 2.0 Authorization Code Flow

```
1. Frontend -> GET /oauth2/authorize?
   response_type=code&
   client_id=client&
   redirect_uri=http://localhost:3001/authorized&
   scope=openid

2. Auth Server -> Redirect to /login (если не аутентифицирован)

3. User -> POST /login (username + password)

4. Auth Server -> Redirect to /oauth2/authorize (повторный запрос)

5. Auth Server -> Redirect to redirect_uri?code=AUTHORIZATION_CODE

6. Frontend -> POST /oauth2/token
   grant_type=authorization_code&
   code=AUTHORIZATION_CODE&
   redirect_uri=http://localhost:3001/authorized

7. Auth Server -> Response: {
   access_token: "JWT_TOKEN",
   id_token: "ID_TOKEN",
   token_type: "Bearer",
   expires_in: 3600
}
```

### 4. Logout

**Endpoint:** `POST /connect/logout`

**Процесс:**

1. OIDC logout request
2. `OidcClearCookiesLogoutHandler` очищает cookies:
    - `XSRF-TOKEN`
    - `JSESSIONID`
3. Инвалидация сессии
4. Редирект на frontend

## UI компоненты

### Thymeleaf шаблоны

**login.html** - Форма входа

- Username input
- Password input
- Ссылка на регистрацию
- CSRF protection

**register.html** - Форма регистрации

- Username input (с валидацией)
- Password input
- Password confirmation
- Отображение ошибок валидации
- CSRF protection

**error.html** - Страница ошибок

- HTTP status code
- Error message
- Ссылка на frontend

### Статические ресурсы

- `/static/styles/` - CSS стили
- `/static/scripts/` - JavaScript
- `/static/images/` - Изображения
- `/static/fonts/` - Шрифты

## Валидация

### Custom validators

`@EqualPasswords` - Проверка совпадения паролей

```java

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EqualPasswordsValidator.class)
public @interface EqualPasswords {
  String message() default "Passwords should be equal";
}
```

`@NoWhitespace` - Проверка отсутствия пробелов

```java

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoWhitespaceValidator.class)
public @interface NoWhitespace {
  String message() default "Can not contain whitespaces";
}
```

## База данных

### Flyway миграции

Расположение: `src/main/resources/db/migration/rangiffler-auth/`

## Запуск

### Требования

- Java 21
- MySQL 8.0
- Gradle 9.2.1

### Локальный запуск

```bash
# 1. Запустить MySQL
bash localenv.sh

# 2. Запустить приложение
cd rfr-auth
../gradlew bootRun

# Или через IDE
# Main class: io.student.rangiffler.RangifflerAuthApplication
```

### Проверка работоспособности

```bash
# OpenID Configuration
curl http://localhost:9001/.well-known/openid-configuration
```

## Troubleshooting

### Проблема: JWT токены невалидны после перезапуска

**Причина:** RSA ключи генерируются заново при каждом старте

### Проблема: CORS ошибки

**Причина:** Frontend на другом домене/порту (не localhost:3001)

**Решение:** Настроить `CorsCustomizer` с правильными origins
