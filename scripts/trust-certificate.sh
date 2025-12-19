#!/bin/bash

# Скрипт для добавления Root CA сертификата в доверенные (macOS Keychain)
# Использование: ./trust-certificate.sh

CERT_DIR="src/main/resources/certs"
ROOT_CA_CERT="$CERT_DIR/root-ca.crt"

if [ ! -f "$ROOT_CA_CERT" ]; then
    echo "❌ Ошибка: файл $ROOT_CA_CERT не найден!"
    echo "Сначала запустите скрипт generate-cert-chain.sh"
    exit 1
fi

echo "Добавление Root CA сертификата в доверенные (macOS Keychain)..."

# Импорт в системный Keychain
sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain "$ROOT_CA_CERT"

if [ $? -eq 0 ]; then
    echo "✅ Root CA сертификат успешно добавлен в доверенные!"
    echo "Браузер теперь будет доверять сертификатам, подписанным этим CA."
else
    echo "❌ Ошибка при добавлении сертификата. Возможно, требуется пароль администратора."
    exit 1
fi

