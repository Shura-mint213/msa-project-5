
# Задание 3. Реализация Distributed Scheduling с k8s CronJob  
**Решение на C# (.NET 8)**

## Технологический стек
- Язык: **C# 12 + .NET 8.0**
- Библиотека: **Npgsql** для работы с PostgreSQL
- Контейнеризация: **Docker**
- Оркестрация: **Kubernetes CronJob + Job** (Minikube)
- Сборка: Single-file self-contained executable

## Результат выполнения
```
Запуск экспорта данных из PostgreSQL...
УСПЕШНО: Экспортировано 20 строк в /tmp/export_20251202_113423.csv
```

[Результат](./result.log)

## Структура проекта и ключевые файлы

| Файл                                                                                                      | Описание                                                          | Ссылка                                                         |
| --------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- | -------------------------------------------------------------- |
| [`Program.cs`](Program.cs)                                                                                | Основной код: подключение к БД, запрос `pg_tables`, экспорт в CSV | [Открыть](Program.cs)                                          |
| [`ConsoleAppTest.csproj`](ConsoleAppTest.csproj)                                                          | Конфигурация проекта: single-file, self-contained, linux-x64      | [Открыть](ConsoleAppTest.csproj)                               |
| [`Dockerfile`](Dockerfile)                                                                                | Минимальный образ на базе `mcr.microsoft.com/dotnet/runtime:8.0`  | [Открыть](Dockerfile)                                          |
| [`build-and-load.sh`](build-and-load.sh)                                                                  | Скрипт сборки + загрузки образа в Minikube одной командой         | [Открыть](build-and-load.sh)                                   |
| [`cronjob.yaml`](cronjob.yaml)                                                                            | Kubernetes CronJob (расписание `*/5 * * * *` для теста)           | [Открыть](cronjob.yaml)                                        |
| [`secret.yaml`](secret.yaml)                                                                              | Секрет к PostgreSQL                                               | [Открыть](secret.yaml)                                         |
| [`postgres-deployment.yaml`](postgres-deployment.yaml) + [`postgres-service.yaml`](postgres-service.yaml) | Локальный PostgreSQL в Minikube (для теста)                       | [Dep](postgres-deployment.yaml) / [Ser](postgres-service.yaml) |

## Демонстрация работы

### 1. CronJob создан и активен
[cronJob.log](cronJob.log)
```log
NAME                   SCHEDULE      TIMEZONE   SUSPEND   ACTIVE   LAST SCHEDULE   AGE
daily-export-cronjob   */5 * * * *   <none>     False     0        3m31s           78m
```
[jobs.log](jobs.log)
### 2. Job завершился успешно
```log
NAME                            STATUS     COMPLETIONS   DURATION   AGE
daily-export-cronjob-29411300   Complete   1/1           4s         45s
```

### 3. Логи пода — успех!
[result.log](result.log)
```log
Запуск экспорта данных из PostgreSQL...
Cannot load library libgssapi_krb5.so.2
УСПЕШНО: Экспортировано 20 строк в /tmp/export_20251202_113423.csv

```

### 4. Список подов
[pods.log](pods.log)
```log
NAME                                  READY   STATUS      RESTARTS       AGE
daily-export-cronjob-29411255-l9hgm   0/1     Completed   0              47m
daily-export-cronjob-29411300-z8q6l   0/1     Completed   0              11s
postgres-569b45fcc9-zfccb             1/1     Running     0              49m
prometheus-66959c78dd-8bjh7           1/1     Running     1 (168m ago)   33d
scaletestapp-65d9585956-rgpj6         1/1     Running     1 (168m ago)   33d
```

## Как запустить локально (Minikube)

```bash
./build-and-load.sh
kubectl apply -f secret.yaml
kubectl apply -f cronjob.yaml
```

Через 1–5 минут:
```bash
kubectl get jobs
kubectl logs -l job-name=debug-export
```