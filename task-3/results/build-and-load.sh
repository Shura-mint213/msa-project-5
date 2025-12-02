#!/bin/bash
set -e

echo "Очистка старой папки publish..."
rm -rf publish

echo "Сборка .NET 8 приложения..."

dotnet publish *.csproj -c Release -o publish --self-contained true -p:PublishSingleFile=true -r linux-x64
# dotnet publish ConsoleAppTest.csproj -c Release -o publish \
#   --self-contained true\
#   -p:PublishSingleFile=true \
#   -p:PublishTrimmed=false \
#   -p:IncludeNativeLibrariesForSelfExtract=true \
#   --runtime linux-x64

echo "Сборка Docker-образа..."
docker build -t daily-exporter:latest .

echo "Загрузка в Minikube..."
minikube image load daily-exporter:latest

echo "Готово! Применяем манифесты:"
kubectl apply -f secret.yaml
kubectl apply -f cronjob.yaml
