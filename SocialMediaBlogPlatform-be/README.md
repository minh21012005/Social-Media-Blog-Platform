# Social Media Blog Platform Backend

Spring Boot and Spring Cloud microservices base for a Medium-like social blogging platform.

## Structure

- `infra/api-gateway`: edge gateway, CORS, JWT validation, service routes.
- `infra/config-server`: centralized native configuration server.
- `infra/discovery-server`: Eureka service discovery server.
- `services/user-service`: user accounts, profiles, registration, login, JWT issuing, refresh tokens, logout.
- `services/article-service`: article bounded context base.
- `services/comment-service`: comment bounded context base.
- `services/interaction-service`: likes/claps bounded context base.
- `services/follower-service`: social graph bounded context base.
- `services/notification-service`: notification bounded context for event-driven notifications.
- `common/common-events`: shared event contracts.
- `common/common-security`: shared JWT, gateway header, and current-user contracts.
- `common/common-web`: shared API response contracts.

## Local Runtime

Start PostgreSQL. The local compose file creates one PostgreSQL instance with separate databases per service:

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
.\mvnw.cmd -pl services/comment-service -am spring-boot:run
.\mvnw.cmd -pl services/interaction-service -am spring-boot:run
.\mvnw.cmd -pl services/follower-service -am spring-boot:run
.\mvnw.cmd -pl services/notification-service -am spring-boot:run
```

Local development values are provided in `.env`. Spring Boot imports this file automatically when services are started with Maven from this repository. The checked-in RSA keys under `config/jwt/` are for local development only.

Default ports:

- API Gateway: `8080`
- Config Server: `8888`
- Discovery Server: `8761`
- User Service: `8081`
- Article Service: `8082`
- Comment Service: `8083`
- Interaction Service: `8084`
- Follower Service: `8085`
- Notification Service: `8086`
- PostgreSQL: `5432`

## Auth Endpoints

- `POST http://localhost:8080/api/v1/auth/register`
- `POST http://localhost:8080/api/v1/auth/login`
- `POST http://localhost:8080/api/v1/auth/refresh` using the HttpOnly `refresh_token` cookie.
- `POST http://localhost:8080/api/v1/auth/logout` clears and revokes the HttpOnly `refresh_token` cookie.
- `GET http://localhost:8080/api/v1/users/me` with `Authorization: Bearer <token>`
- `PATCH http://localhost:8080/api/v1/users/me` with `Authorization: Bearer <token>`
- `POST http://localhost:8080/api/v1/users/me/change-password` with `Authorization: Bearer <token>`

Register, login, and refresh return the access token in the response body and set the refresh token as an HttpOnly cookie.

## Important Environment Variables

- `JWT_PRIVATE_KEY_PATH`: RS256 private key path used only by `user-service`, default `./config/jwt/dev-private-key.pem`.
- `JWT_PUBLIC_KEY_PATH`: RS256 public key path used by gateway/services to verify JWTs, default `./config/jwt/dev-public-key.pem`.
- `JWT_KEY_ID`: JWT key id header value, default `social-blog-local-dev`.
- `JWT_ISSUER`: token issuer, default `social-media-blog-platform`.
- `JWT_ACCESS_TOKEN_TTL`: access token lifetime, default `15m`.
- `JWT_REFRESH_TOKEN_TTL`: refresh token lifetime, default `7d`.
- `REFRESH_TOKEN_COOKIE_NAME`: refresh cookie name, default `refresh_token`.
- `REFRESH_TOKEN_COOKIE_PATH`: refresh cookie path, default `/api/v1/auth`.
- `REFRESH_TOKEN_COOKIE_SECURE`: set `true` behind HTTPS, default `false` for local HTTP.
- `REFRESH_TOKEN_COOKIE_SAME_SITE`: default `Lax`; use `None` with `Secure=true` for cross-site deployments.
- `USER_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_users`.
- `USER_SERVICE_DB_USERNAME`: default `social_blog`.
- `USER_SERVICE_DB_PASSWORD`: default `social_blog`.
- `ARTICLE_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_articles`.
- `COMMENT_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_comments`.
- `INTERACTION_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_interactions`.
- `FOLLOWER_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_followers`.
- `NOTIFICATION_SERVICE_DB_URL`: default `jdbc:postgresql://localhost:5432/social_blog_notifications`.
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`: Cloudinary credentials used by user/article media storage adapters.
- `CLOUDINARY_AVATAR_FOLDER`: default `social-blog/avatars`.
- `CLOUDINARY_ARTICLE_FOLDER`: default `social-blog/articles`.
- `CORS_ALLOWED_ORIGINS`: default `http://localhost:5173`.

## Checks

```powershell
.\mvnw.cmd test
```

## Production

See [PRODUCTION_DEPLOYMENT.md](../PRODUCTION_DEPLOYMENT.md) for deployment on subtrack.click and api.subtrack.click.
