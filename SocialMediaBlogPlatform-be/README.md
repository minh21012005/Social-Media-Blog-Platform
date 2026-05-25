# Social Media Blog Platform Backend

Spring Boot and Spring Cloud microservices base for a Medium-like social blogging platform.

## Structure

- `infra/api-gateway`: edge gateway, CORS, JWT validation, service routes.
- `infra/config-server`: centralized native configuration server.
- `infra/discovery-server`: Eureka service discovery server.
- `services/user-service`: user accounts, profiles, registration, login, JWT issuing.
- `services/article-service`: article service placeholder for the next iteration.
- `common/common-events`: shared event contracts.
- `common/common-security`: shared JWT/current-user contracts.
- `common/common-web`: shared API response contracts.

## Local Runtime

Start PostgreSQL:

```powershell
docker compose up -d
```

Run infrastructure and services:

```powershell
.\mvnw.cmd -pl infra/config-server spring-boot:run
.\mvnw.cmd -pl infra/discovery-server spring-boot:run
.\mvnw.cmd -pl infra/api-gateway spring-boot:run
.\mvnw.cmd -pl services/user-service -am spring-boot:run
.\mvnw.cmd -pl services/article-service -am spring-boot:run
```

Default ports:

- API Gateway: `8080`
- Config Server: `8888`
- Discovery Server: `8761`
- User Service: `8081`
- Article Service: `8082`
- User PostgreSQL: `5432`

## Auth Endpoints

- `POST http://localhost:8080/api/v1/auth/register`
- `POST http://localhost:8080/api/v1/auth/login`
- `GET http://localhost:8080/api/v1/users/me` with `Authorization: Bearer <token>`

## Important Environment Variables

- `JWT_SECRET`: shared HS256 secret, at least 32 bytes.
- `JWT_ISSUER`: token issuer, default `social-media-blog-platform`.
- `USER_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_users`.
- `USER_SERVICE_DB_USERNAME`: default `social_blog`.
- `USER_SERVICE_DB_PASSWORD`: default `social_blog`.
- `CORS_ALLOWED_ORIGINS`: default `http://localhost:5173`.

## Checks

```powershell
.\mvnw.cmd test
```
