# Media CMS — инструкция по проверке API и требований по БД

Бэкенд: Spring Boot 3, Java 17  
Базы данных:
- PostgreSQL — для пользователей и контента (статьи, видео, подкасты)
- MongoDB — для комментариев и HTTP-сессий (Spring Session)

Приложение (по умолчанию) поднимается на:  
`http://localhost:8080`

0. Запуск приложения и Docker Compose

Требования:

Docker + Docker Compose

JDK 17

Maven (для запуска тестов и сборки отчёта JaCoCo)

0.1. Поднять базы данных

В корне репозитория есть docker-compose.yml. Для запуска Postgres и MongoDB:
docker compose up -d

PostgreSQL и MongoDB поднимаются с параметрами, которые используются в src/main/resources/application.yml
(логин/пароль и имя базы берутся оттуда).

0.2. Запустить Spring Boot приложение

Варианты:

Через Maven:
mvn spring-boot:run
Или:
mvn clean package
java -jar target/media-cms-0.0.1-SNAPSHOT.jar
Или: запустить класс MediaCmsApplication из IDE (IntelliJ IDEA).

Тесты:

ArticleControllerTest — CRUD для статей.

CommentControllerTest — создание/чтение/обновление/удаление комментариев и вложенных ответов, обработка ошибок.

SessionDemoControllerTest — счётчик обращений в HTTP-сессии (MongoDB + Spring Session).

VideoControllerTest — базовый сценарий получения списка видео.

PodcastControllerTest — базовый сценарий получения списка подкастов.

UserControllerTest — операции с пользователями: список, получение по id, обновление (логин/пароль/роль), удаление и проверки ошибок.

Запуск тестов:
mvn clean test



---

## 1. Swagger и OpenAPI

### 1.1. Описание API (OpenAPI JSON)


curl -v http://localhost:8080/v3/api-docs
# Ожидаем: HTTP/1.1 200 OK и JSON с описанием API

Swagger UI (проверяется в браузере)
http://localhost:8080/swagger-ui/index.html

2. Регистрация и авторизация пользователей (Basic Auth)
2.1. Регистрация нового пользователя (открытый эндпоинт)

curl -v \
  -H "Content-Type: application/json; charset=utf-8" \
  --data-binary @- \
  http://localhost:8080/api/auth/register << 'EOF'
{
  "username": "user1",
  "password": "pass1"
}
EOF
# Ожидаем: 200 OK и JSON вида:
# {"id":2,"username":"user1","role":"USER"}

Ещё один пользователь:
curl -v \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/auth/register << 'EOF'
{
"username": "user2",
"password": "pass2"
}
EOF
# Ожидаем: 200 OK, {"id":3,"username":"user2","role":"USER"}

2.2. Текущий авторизованный пользователь /api/auth/me

curl -u admin:admin http://localhost:8080/api/auth/me
# Ожидаем: {"id":1,"username":"admin","role":"ADMIN"}

Проверка под обычным пользователем:
curl -u user1:pass1 http://localhost:8080/api/auth/me
# Ожидаем: {"id":2,"username":"user1","role":"USER"}

3. Доступ к контенту и проверка ролей
   3.1. Публичный просмотр контента (без авторизации)
   curl -v http://localhost:8080/api/articles
   curl -v http://localhost:8080/api/videos
   curl -v http://localhost:8080/api/podcasts
# Ожидаем: HTTP/1.1 200 OK и JSON (даже без логина)

3.2. Создание статей / видео / подкастов
Попытка создать статью обычным пользователем — должно быть запрещено

curl -v -u user1:pass1 \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/articles << 'EOF'
{
"title": "Статья от обычного юзера",
"text": "Контент",
"author": "user1",
"publishedAt": "2025-12-10"
}
EOF
# Ожидаем: 403 Forbidden

Создание статьи админом

curl -v -u admin:admin \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/articles << 'EOF'
{
"title": "Админская статья",
"text": "Админ правит контент",
"author": "Admin",
"publishedAt": "2025-12-10"
}
EOF
# Ожидаем: 200 OK и JSON с полем id

Создание видео админом

curl -v -u admin:admin \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/videos << 'EOF'
{
"title": "Admin video",
"url": "https://example.com/admin-video",
"duration": 1234
}
EOF
# Ожидаем: 200 OK и JSON с id

Создание подкаста админом

curl -v -u admin:admin \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/podcasts << 'EOF'
{
"title": "Admin podcast",
"audioUrl": "https://example.com/admin-podcast",
"episodes": [
"Эпизод 1",
"Эпизод 2"
]
}
EOF
# Ожидаем: 200 OK и JSON с id


3.3. Обновление и удаление статей (только ADMIN)
Обновление статьи id=1

curl -v -u admin:admin \
-X PUT \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/articles/1 << 'EOF'
{
"title": "Обновлённая админская статья",
"text": "Новый контент",
"author": "Admin",
"publishedAt": "2025-12-11"
}
EOF
# Ожидаем: 200 OK и обновлённый JSON статьи

Удаление статьи id=1
curl -v -u admin:admin -X DELETE http://localhost:8080/api/articles/1
# Ожидаем: 204 No Content

4. Управление пользователями (только ADMIN)
   4.1. Список всех пользователей

curl -v -u admin:admin http://localhost:8080/api/users
# Ожидаем: 200 OK и массив пользователей

4.2. Попытка получить список пользователей под обычным юзером — запрещено

curl -v -u user1:pass1 http://localhost:8080/api/users
# Ожидаем: 403 Forbidden

5. Комментарии и MongoDB
   Комментарии иерархические, хранятся в MongoDB, привязаны к любому типу контента:

contentType: ARTICLE, VIDEO или PODCAST

contentId: ID соответствующего объекта (из PostgreSQL)

Все операции с комментариями требуют авторизации (любой залогиненный пользователь).

5.1. Попытка создать комментарий анонимно — должно быть запрещено

curl -v \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/comments << 'EOF'
{
"contentType": "ARTICLE",
"contentId": 2,
"text": "Попытка анонима"
}
EOF
# Ожидаем: 401 Unauthorized (Basic Auth)

5.2. Создать комментарий к статье id=2 под user1

curl -v -u user1:pass1 \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/comments << 'EOF'
{
"contentType": "ARTICLE",
"contentId": 2,
"text": "Первый комментарий к статье 2"
}
EOF
# Ожидаем: 200 OK и JSON вида:
# {
#   "id":"<Mongo ObjectId>",
#   "contentType":"ARTICLE",
#   "contentId":2,
#   "authorUsername":"user1",
#   "text":"Первый комментарий к статье 2",
#   "createdAt": "...",
#   "updatedAt": "...",
#   "replies":[],
#   "metadata":{}
# }

Запоминаем id из ответа

5.3. Получить все комментарии по контенту (по ARTICLE + contentId)

curl -v -u user1:pass1 \
"http://localhost:8080/api/comments/by-content?type=ARTICLE&contentId=2"
# Ожидаем: 200 OK и массив комментариев к статье 2

5.4. Получить комментарий по id

curl -v -u user1:pass1 \
http://localhost:8080/api/comments/69395086bb0a867b74cd695c
# Ожидаем: 200 OK и JSON этого комментария

5.5. Обновить текст комментария

curl -v -u user1:pass1 \
-X PUT \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/comments/69395086bb0a867b74cd695c << 'EOF'
{
"text": "Обновлённый текст комментария"
}
EOF
# Ожидаем: 200 OK и обновлённый JSON комментария

5.6. Добавить вложенный ответ (reply) к комментарию

curl -v -u user1:pass1 \
-X POST \
-H "Content-Type: application/json; charset=utf-8" \
--data-binary @- \
http://localhost:8080/api/comments/69395086bb0a867b74cd695c/replies << 'EOF'
{
"text": "Ответ на комментарий",
"metadata": {
"liked": true
}
}
EOF
# Ожидаем: 200 OK и JSON корневого комментария с заполненным массивом replies

5.7. Удалить комментарий (ADMIN)

curl -v -u admin:admin -X DELETE \
http://localhost:8080/api/comments/69395086bb0a867b74cd695c
# Ожидаем: 200 OK или 204 No Content (в зависимости от реализации)

Проверка обработки ошибки (необязательная, но показывает корректный 404):

curl -v -u admin:admin -X DELETE \
http://localhost:8080/api/comments/1
# Ожидаем: 404 Not Found и JSON:
# {"error":"COMMENT_NOT_FOUND","message":"Comment not found: 1"}

6. Сессии в MongoDB (Spring Session)

Для демонстрации хранения HTTP-сессий в MongoDB используется эндпоинт GET /api/session-demo/hit

6.1. Первый запрос — создаём сессию и сохраняем cookies

curl -c cookies.txt http://localhost:8080/api/session-demo/hit
# Ожидаем: 200 OK и JSON вида:
# {"sessionId":"...","hits":1}

6.2. Второй запрос с теми же cookies — hits должен увеличиться

curl -b cookies.txt http://localhost:8080/api/session-demo/hit
# Ожидаем: 200 OK и JSON вида:
# {"sessionId":"тот же id","hits":2}


## Тестирование и покрытие

Проект настроен на использование JUnit 5, Mockito и JaCoCo.

Тесты:
- `ArticleControllerTest` — базовый CRUD для статей
- `CommentControllerTest` — создание комментариев и ответов, обработка ошибок
- `SessionDemoControllerTest` — демонстрация работы сессий (MongoDB + Spring Session)

Запуск тестов (на стороне проверяющего):

```bash
mvn clean test

