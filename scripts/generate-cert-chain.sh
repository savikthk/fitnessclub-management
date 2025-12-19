#!/bin/bash

# Скрипт для генерации цепочки сертификатов (CA -> Intermediate -> Server)
# Использование: ./generate-cert-chain.sh <STUDENT_ID>
# Пример: ./generate-cert-chain.sh "ST-2024-12345"

STUDENT_ID=${1:-"ST-2024-12345"}
KEYSTORE_PASSWORD=${2:-"changeit"}

CERT_DIR="src/main/resources/certs"
KEYSTORE_FILE="$CERT_DIR/fitnessclub-keystore.p12"

echo "Генерация цепочки сертификатов для студента: $STUDENT_ID"

# Создаем директорию для сертификатов
mkdir -p "$CERT_DIR"

# 1. Генерация Root CA (самоподписанный)
echo "Шаг 1: Генерация Root CA..."
keytool -genkeypair -alias root-ca \
    -keyalg RSA -keysize 2048 \
    -dname "CN=FitnessClub Root CA, OU=IT Department, O=FitnessClub Inc, L=Moscow, ST=Moscow, C=RU, SERIALNUMBER=$STUDENT_ID" \
    -ext "SAN=email:fitnessclub-ca@example.com" \
    -keystore "$KEYSTORE_FILE" \
    -storetype PKCS12 \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -validity 3650

# Экспорт Root CA сертификата
keytool -exportcert -alias root-ca \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -file "$CERT_DIR/root-ca.crt"

# 2. Генерация Intermediate CA (подписан Root CA)
echo "Шаг 2: Генерация Intermediate CA..."
keytool -genkeypair -alias intermediate-ca \
    -keyalg RSA -keysize 2048 \
    -dname "CN=FitnessClub Intermediate CA, OU=IT Department, O=FitnessClub Inc, L=Moscow, ST=Moscow, C=RU, SERIALNUMBER=$STUDENT_ID" \
    -ext "SAN=email:fitnessclub-intermediate@example.com" \
    -keystore "$KEYSTORE_FILE" \
    -storetype PKCS12 \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -validity 1825

# Создание запроса на сертификат для Intermediate CA
keytool -certreq -alias intermediate-ca \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -file "$CERT_DIR/intermediate-ca.csr"

# Подписание Intermediate CA сертификата Root CA
keytool -gencert -alias root-ca \
    -infile "$CERT_DIR/intermediate-ca.csr" \
    -outfile "$CERT_DIR/intermediate-ca.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -validity 1825 \
    -ext "SAN=email:fitnessclub-intermediate@example.com"

# Импорт Root CA сертификата в keystore (если еще не импортирован)
keytool -list -alias root-ca -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASSWORD" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    keytool -importcert -alias root-ca \
        -file "$CERT_DIR/root-ca.crt" \
        -keystore "$KEYSTORE_FILE" \
        -storepass "$KEYSTORE_PASSWORD" \
        -noprompt
fi

# Импорт Intermediate CA сертификата
keytool -importcert -alias intermediate-ca \
    -file "$CERT_DIR/intermediate-ca.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -trustcacerts \
    -noprompt

# 3. Генерация Server сертификата (подписан Intermediate CA)
echo "Шаг 3: Генерация Server сертификата..."

# Удаляем старый алиас server, если есть
keytool -delete -alias server -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASSWORD" > /dev/null 2>&1

# Создаем новую ключевую пару для сервера с приватным ключом
keytool -genkeypair -alias server \
    -keyalg RSA -keysize 2048 \
    -dname "CN=localhost, OU=IT Department, O=FitnessClub Inc, L=Moscow, ST=Moscow, C=RU, SERIALNUMBER=$STUDENT_ID" \
    -ext "SAN=DNS:localhost,DNS:*.localhost,IP:127.0.0.1,IP:0.0.0.0" \
    -keystore "$KEYSTORE_FILE" \
    -storetype PKCS12 \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -validity 365

# Создаем запрос на сертификат
keytool -certreq -alias server \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -file "$CERT_DIR/server.csr"

# Подписываем сертификат промежуточным CA
keytool -gencert -alias intermediate-ca \
    -infile "$CERT_DIR/server.csr" \
    -outfile "$CERT_DIR/server.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -validity 365 \
    -ext "SAN=DNS:localhost,DNS:*.localhost,IP:127.0.0.1,IP:0.0.0.0"

# Импортируем подписанный сертификат обратно в алиас server
keytool -importcert -alias server \
    -file "$CERT_DIR/server.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -noprompt

# Удаляем старый алиас server (если есть), чтобы создать новый
keytool -delete -alias server -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASSWORD" > /dev/null 2>&1

# Создаем новую ключевую пару для сервера (с приватным ключом)
keytool -genkeypair -alias server \
    -keyalg RSA -keysize 2048 \
    -dname "CN=localhost, OU=IT Department, O=FitnessClub Inc, L=Moscow, ST=Moscow, C=RU, SERIALNUMBER=$STUDENT_ID" \
    -ext "SAN=DNS:localhost,DNS:*.localhost,IP:127.0.0.1,IP:0.0.0.0" \
    -keystore "$KEYSTORE_FILE" \
    -storetype PKCS12 \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -validity 365

# Создаем запрос на сертификат
keytool -certreq -alias server \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -file "$CERT_DIR/server.csr"

# Подписываем сертификат промежуточным CA
keytool -gencert -alias intermediate-ca \
    -infile "$CERT_DIR/server.csr" \
    -outfile "$CERT_DIR/server.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -validity 365 \
    -ext "SAN=DNS:localhost,DNS:*.localhost,IP:127.0.0.1,IP:0.0.0.0"

# Импортируем подписанный сертификат обратно в алиас server
# Важно: используем тот же алиас, где уже есть приватный ключ
keytool -importcert -alias server \
    -file "$CERT_DIR/server.crt" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$KEYSTORE_PASSWORD" \
    -noprompt