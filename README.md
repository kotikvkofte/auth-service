# Auth Service

Сервис аутентификации и авторизации на Spring Boot с использованием JWT и Docker.

## Запуск в Docker


### Настройка

Создайте `.env` файл в корне проекта со следующим содержимым:

```env
DB_NAME=auth_db
DB_USER=postgres
DB_PASSWORD=postgres
```

### Запуск

```bash
docker-compose up --build
```

Сервис будет доступен на:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### Основные эндпоинты

- `PUT /auth/signup` — регистрация
- `POST /auth/signin` — вход (JWT)
- `PUT /auth/user-roles/save` — назначение ролей (только ADMIN)
- `GET /auth/user-roles/{login}` — просмотр ролей

---

