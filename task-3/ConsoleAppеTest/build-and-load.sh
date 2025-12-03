#!/bin/bash
set -e

echo "Сборка .NET приложения..."
dotnet publish -c Release -o publish

echo "Сборка Docker-образа..."
docker build -t daily-exporter:latest .

echo "Загрузка образа в Minikube..."
minikube image load daily-exporter:latest

echo "Готово! Теперь применяем манифесты:"
echo "kubectl apply -f secret.yaml"
echo "kubectl apply -f cronjob.yaml"