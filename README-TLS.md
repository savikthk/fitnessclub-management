# Настройка TLS для FitnessClub

## Генерация цепочки сертификатов

Для генерации цепочки сертификатов (Root CA -> Intermediate CA -> Server) выполните:

```bash
cd /Users/savik/java-projects/fitnessclub/fitnessclub
./scripts/generate-cert-chain.sh <STUDENT_ID> [KEYSTORE_PASSWORD]
```

**Пример:**
```bash
./scripts/generate-cert-chain.sh "ST-2024-12345" "mySecurePassword123"
```

Если пароль не указан, будет использован пароль по умолчанию: `changeit`

### Структура цепочки:

1. **Root CA** (`root-ca`) - самоподписанный сертификат, действителен 10 лет
2. **Intermediate CA** (`intermediate-ca`) - подписан Root CA, действителен 5 лет
3. **Server** (`server`) - подписан Intermediate CA, действителен 1 год

Все сертификаты содержат идентификатор студента в поле `SERIALNUMBER` (Subject DN).

## Добавление сертификата в доверенные (macOS)

После генерации сертификатов добавьте Root CA в системный Keychain:

```bash
./scripts/trust-certificate.sh
```

Это позволит браузеру доверять сертификатам, подписанным нашим Root CA.

**Примечание:** Требуются права администратора.

## Настройка Spring Boot для TLS

Приложение настроено на работу с HTTPS на порту **8443**.

Конфигурация находится в `application.properties`:

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:certs/fitnessclub-keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD:changeit}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=server
```

Пароль keystore можно задать через переменную окружения `KEYSTORE_PASSWORD`:

```bash
export KEYSTORE_PASSWORD=mySecurePassword123
mvn spring-boot:run
```

## Запуск приложения

После генерации сертификатов запустите приложение:

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: **https://localhost:8443**

## GitHub Secrets для CI/CD

Для работы CI/CD pipeline необходимо настроить следующие секреты в GitHub:

1. Перейдите в Settings → Secrets and variables → Actions
2. Добавьте секрет:
   - **Name:** `KEYSTORE_PASSWORD`
   - **Value:** пароль для keystore (используйте тот же, что при генерации)

CI pipeline автоматически:
- Генерирует цепочку сертификатов
- Компилирует проект
- Запускает тесты
- Создаёт JAR артефакт
- Загружает артефакты в GitHub Actions

## Проверка сертификата

После запуска приложения откройте в браузере:

```
https://localhost:8443/api/auth/signin
```

Если сертификат добавлен в доверенные, браузер не будет показывать предупреждение о небезопасном соединении.

Для проверки цепочки сертификатов:

```bash
keytool -list -v -keystore src/main/resources/certs/fitnessclub-keystore.p12 -storepass changeit
```

