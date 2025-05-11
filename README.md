# CoinFlow

**CoinFlow** — сервис для учёта личных финансов, категорий расходов, бюджетирования и анализа транзакций.

---

## Требования

- **Java 17+**
- **Maven 3.6+**
- **PostgreSQL 15+**
- (Для Docker-режима: Docker и Docker Compose)

---

## 1. Локальный запуск (без Docker)

### 1.1. Клонируйте репозиторий

```bash
git clone <URL_ВАШЕГО_РЕПОЗИТОРИЯ>
cd Курсач
```

### 1.2. Настройте базу данных

Создайте базу данных PostgreSQL (например, `coinflow`):

```sql
CREATE DATABASE coinflow;
CREATE USER coinflow_user WITH PASSWORD 'coinflow_pass';
GRANT ALL PRIVILEGES ON DATABASE coinflow TO coinflow_user;
```

### 1.3. Настройте `src/main/resources/application.properties`

Пример:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/coinflow
spring.datasource.username=coinflow_user
spring.datasource.password=coinflow_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 1.4. Соберите и запустите приложение

```bash
./mvnw clean package
java -jar target/CoinFlow-0.0.1-SNAPSHOT.jar
```

---

## 2. Запуск через Docker Compose

### 2.1. Соберите jar-файл приложения

```bash
./mvnw clean package
```

### 2.2. Соберите Docker-образ приложения

```bash
docker build -t tserenov/coinflow-app:latest .
```

### 2.3. Запустите все сервисы

```bash
docker-compose up -d
```

- Будут подняты:
  - **PostgreSQL** (порт 5432)
  - **pgAdmin** (порт 5050, логин: admin@admin.com, пароль: admin)
  - **CoinFlow** (порт 8080)

### 2.4. Остановить сервисы

```bash
docker-compose down
```

---

## 3. Доступ к сервису

- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- pgAdmin: [http://localhost:5050](http://localhost:5050) (логин: admin@admin.com, пароль: admin)

---

## 4. Примеры запросов

- Регистрация пользователя: `POST /auth/sign-up`
- Авторизация: `POST /auth/sign-in`
- Работа с транзакциями: `POST /transactions`, `GET /transactions`, и т.д.
- Работа с категориями: `POST /categories`, `GET /categories`, и т.д.

---

## 5. Переменные окружения (Docker)

В `docker-compose.yml` можно изменить параметры подключения к БД и другие настройки через секцию `environment`.

---

## 6. Примечания

- После первого запуска приложение автоматически создаст стандартные категории.
- Для отправки email (например, сброс пароля) настройте параметры SMTP в `application.properties`.
