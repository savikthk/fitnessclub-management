# Branch Configurations

Эта папка содержит конфигурации для разных демо-веток проекта.

## Структура

```
branch-configs/
├── basic-auth/           # Basic HTTP авторизация
│   ├── SecurityConfig.java
│   └── FitnessClub_BasicAuth_Collection.json
├── csrf-enabled/         # CSRF защита включена
│   ├── SecurityConfig.java
│   ├── CsrfController.java
│   └── FitnessClub_CSRF_Collection.json
└── jwt-tokens/           # JWT токены
    ├── SecurityConfig.java
    └── FitnessClub_JWT_Collection.json
```

## Как использовать

### 1. Basic Auth (ветка demo/basic-auth)

Скопируйте файлы в проект:
```bash
cp branch-configs/basic-auth/SecurityConfig.java src/main/java/com/fitnessclub/security/
cp branch-configs/basic-auth/FitnessClub_BasicAuth_Collection.json postman/
```

**Авторизация:** Логин и пароль передаются в заголовке Authorization:
```
Authorization: Basic base64(username:password)
```

### 2. CSRF Enabled (ветка demo/csrf-enabled)

Скопируйте файлы в проект:
```bash
cp branch-configs/csrf-enabled/SecurityConfig.java src/main/java/com/fitnessclub/security/
cp branch-configs/csrf-enabled/CsrfController.java src/main/java/com/fitnessclub/controller/
cp branch-configs/csrf-enabled/FitnessClub_CSRF_Collection.json postman/
```

**Авторизация:** Basic Auth + CSRF токен для POST/PUT/DELETE запросов:
1. Получить токен: `GET /api/csrf`
2. Передать в заголовке: `X-XSRF-TOKEN: <token>`

### 3. JWT Tokens (ветка demo/jwt-tokens)

Скопируйте файлы в проект:
```bash
cp branch-configs/jwt-tokens/SecurityConfig.java src/main/java/com/fitnessclub/security/
cp branch-configs/jwt-tokens/FitnessClub_JWT_Collection.json postman/
```

**Авторизация:** JWT токены:
1. Логин: `POST /api/auth/login` → получаем access + refresh токены
2. Защищённые эндпоинты: `Authorization: Bearer <accessToken>`
3. Обновление: `POST /api/auth/refresh` с refresh токеном

## Создание веток

```bash
# Создать ветку Basic Auth
git checkout main
git checkout -b demo/basic-auth
cp branch-configs/basic-auth/* соответствующие_папки/
git add . && git commit -m "Basic Auth configuration"
git push origin demo/basic-auth

# Создать ветку CSRF
git checkout main
git checkout -b demo/csrf-enabled
cp branch-configs/csrf-enabled/SecurityConfig.java src/main/java/com/fitnessclub/security/
cp branch-configs/csrf-enabled/CsrfController.java src/main/java/com/fitnessclub/controller/
cp branch-configs/csrf-enabled/FitnessClub_CSRF_Collection.json postman/
git add . && git commit -m "CSRF protection enabled"
git push origin demo/csrf-enabled

# Создать ветку JWT
git checkout main
git checkout -b demo/jwt-tokens
cp branch-configs/jwt-tokens/* соответствующие_папки/
git add . && git commit -m "JWT tokens configuration"
git push origin demo/jwt-tokens
```

