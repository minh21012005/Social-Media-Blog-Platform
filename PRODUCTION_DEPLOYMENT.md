# Hướng dẫn triển khai Subtrack lên production

Tài liệu này hướng dẫn triển khai hệ thống với kiến trúc:

- Frontend React/Vite: Vercel tại `https://subtrack.click`
- API Gateway, microservices, Kafka và Redis: Amazon EC2
- API và WebSocket public: `https://api.subtrack.click`
- Database: một Amazon RDS for PostgreSQL private, chứa sáu database logic
- HTTPS reverse proxy: Nginx và Let's Encrypt trên EC2

> Các lệnh Linux trong tài liệu được viết cho Ubuntu trên EC2. Không đưa file `.env.production`, private key JWT hoặc file SQL đã điền password lên Git.

## 1. Kiến trúc sau khi triển khai

```text
Người dùng
    |
    +-- https://subtrack.click ----------------------> Vercel
    |
    +-- https://api.subtrack.click
                    |
                    v
            Elastic IP của EC2
                    |
                  Nginx :443
                    |
          API Gateway 127.0.0.1:8080
                    |
        +-----------+------------+
        |                        |
  Các microservice          WebSocket service
        |
        +------ Kafka / Redis trong Docker
        |
        +------ RDS PostgreSQL private :5432
```

RDS và EC2 phải nằm trong cùng VPC. RDS không được public ra Internet.

## 2. Checklist cần chuẩn bị

Trước khi deploy, cần có:

- Tài khoản AWS.
- Một EC2 chạy Ubuntu và có Elastic IP.
- Một RDS for PostgreSQL private.
- Domain `subtrack.click` và quyền chỉnh DNS.
- Tài khoản Vercel.
- Google OAuth Web Client ID.
- Cloudinary credentials.
- Repository đã được push lên GitHub hoặc Git provider mà EC2/Vercel truy cập được.

Các giá trị production không được để trống:

- Sáu database password.
- `GOOGLE_CLIENT_ID`.
- `CLOUDINARY_CLOUD_NAME`.
- `CLOUDINARY_API_KEY`.
- `CLOUDINARY_API_SECRET`.
- JWT private/public key production.

## 3. Tạo EC2 và cấu hình mạng

### 3.1. Tạo EC2

Cấu hình khởi đầu đề xuất:

- OS: Ubuntu LTS.
- EC2 và RDS cùng Region, cùng VPC.
- Gắn Elastic IP để IP không thay đổi khi reboot.
- Dung lượng disk đủ cho Docker image, Kafka và log.

### 3.2. Security Group của EC2

Inbound rules:

| Port | Nguồn | Mục đích |
|---|---|---|
| `22` | Chỉ IP quản trị của bạn | SSH |
| `80` | `0.0.0.0/0`, `::/0` | HTTP và cấp certificate |
| `443` | `0.0.0.0/0`, `::/0` | HTTPS API/WebSocket |

Không mở các port sau ra Internet:

- `8080`: API Gateway nội bộ sau Nginx.
- `5432`: PostgreSQL.
- `6379`: Redis.
- `9092`, `29092`: Kafka.
- `8761`: Eureka.
- `8888`: Config Server.

File Compose hiện bind các port hạ tầng vào `127.0.0.1`, nhưng Security Group vẫn cần được cấu hình chặt chẽ.

## 4. Tạo RDS PostgreSQL private

### 4.1. Tạo instance

Trong AWS RDS:

1. Chọn PostgreSQL.
2. Chọn cùng VPC với EC2.
3. Đặt `Public access` thành `No`.
4. Bật mã hóa storage.
5. Bật automated backup; giai đoạn đầu có thể đặt retention khoảng 7 ngày.
6. Lưu master username/password trong password manager; application không được dùng master account.
7. Có thể dùng Single-AZ lúc đầu để tiết kiệm; chuyển Multi-AZ khi cần khả năng sẵn sàng cao.

### 4.2. Security Group của RDS

RDS Security Group chỉ cần inbound rule:

| Type | Port | Source |
|---|---|---|
| PostgreSQL | `5432` | Security Group của EC2 |

Không dùng `0.0.0.0/0` và không mở trực tiếp IP cá nhân vào RDS production.

AWS Console có chức năng `Connect to an EC2 compute resource` để tự tạo liên kết Security Group giữa EC2 và RDS.

### 4.3. Cài PostgreSQL client trên EC2

SSH vào EC2:

```bash
sudo apt update
sudo apt install -y postgresql-client
```

Kiểm tra EC2 kết nối được RDS:

```bash
psql "host=<rds-endpoint> port=5432 dbname=postgres user=<rds-master-user> sslmode=require"
```

Nếu timeout, kiểm tra:

- EC2 và RDS có cùng VPC hay không.
- RDS có private DNS endpoint hay không.
- RDS Security Group có cho phép Security Group của EC2 vào port `5432` hay không.
- Network ACL và route table có chặn traffic hay không.

### 4.4. Tạo sáu database và role riêng

Tại thư mục backend trên EC2:

```bash
cp deploy/rds/create-service-databases.sql.example \
  deploy/rds/create-service-databases.sql
```

Mở file vừa copy và thay toàn bộ `CHANGE_ME_*` bằng sáu password mạnh khác nhau. File đã điền password được `.gitignore` bỏ qua.

Chạy script bằng RDS master user:

```bash
psql "host=<rds-endpoint> port=5432 dbname=postgres user=<rds-master-user> sslmode=require" \
  -f deploy/rds/create-service-databases.sql
```

Script tạo:

| Service | Database | Role |
|---|---|---|
| user-service | `social_blog_users` | `user_service` |
| article-service | `social_blog_articles` | `article_service` |
| comment-service | `social_blog_comments` | `comment_service` |
| interaction-service | `social_blog_interactions` | `interaction_service` |
| follower-service | `social_blog_followers` | `follower_service` |
| notification-service | `social_blog_notifications` | `notification_service` |

Kiểm tra sau khi tạo:

```bash
psql "host=<rds-endpoint> port=5432 dbname=postgres user=<rds-master-user> sslmode=require" \
  -c "\l"

psql "host=<rds-endpoint> port=5432 dbname=postgres user=<rds-master-user> sslmode=require" \
  -c "\du"
```

Flyway của từng service chỉ tạo và nâng cấp bảng bên trong database đã tồn tại; Flyway không tạo sáu database này.

## 5. Trỏ DNS

### 5.1. API domain

Tại nhà cung cấp DNS của `subtrack.click`, tạo record:

| Type | Name | Value |
|---|---|---|
| `A` | `api` | Elastic IP của EC2 |

Sau khi DNS cập nhật:

```bash
nslookup api.subtrack.click
```

Kết quả phải trả về Elastic IP của EC2.

### 5.2. Frontend domain

Domain frontend sẽ được thêm trong Vercel ở bước 11. Vercel sẽ hiển thị chính xác record DNS cần tạo cho apex domain `subtrack.click`; dùng record mà Vercel cung cấp thay vì tự đoán.

## 6. Cài Docker Engine và Docker Compose trên EC2

Cài từ repository chính thức của Docker:

```bash
sudo apt update
sudo apt install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
```

Thêm Docker repository:

```bash
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
```

Cài Docker và Compose plugin:

```bash
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io \
  docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Logout rồi SSH lại để group mới có hiệu lực. Kiểm tra:

```bash
docker version
docker compose version
docker run --rm hello-world
```

## 7. Lấy source code trên EC2

Ví dụ:

```bash
cd /opt
sudo mkdir -p subtrack
sudo chown "$USER":"$USER" subtrack
cd subtrack
git clone <repository-url> app
cd app/SocialMediaBlogPlatform-be
```

Nếu repository private, dùng deploy key hoặc GitHub credential phù hợp; không ghi access token trực tiếp vào Git remote URL hoặc shell history.

## 8. Tạo `.env.production`

Trong `SocialMediaBlogPlatform-be`:

```bash
cp .env.production.example .env.production
chmod 600 .env.production
nano .env.production
```

Thay `<rds-endpoint>` trong cả sáu URL và điền đúng role/password đã tạo ở RDS:

```env
USER_SERVICE_DB_URL=jdbc:postgresql://<rds-endpoint>:5432/social_blog_users?sslmode=require
USER_SERVICE_DB_USERNAME=user_service
USER_SERVICE_DB_PASSWORD=<password-user-service>

ARTICLE_SERVICE_DB_URL=jdbc:postgresql://<rds-endpoint>:5432/social_blog_articles?sslmode=require
ARTICLE_SERVICE_DB_USERNAME=article_service
ARTICLE_SERVICE_DB_PASSWORD=<password-article-service>
```

Làm tương tự cho comment, interaction, follower và notification.

Các giá trị domain/cookie phải giữ:

```env
CORS_ALLOWED_ORIGINS=https://subtrack.click
REFRESH_TOKEN_COOKIE_SECURE=true
REFRESH_TOKEN_COOKIE_SAME_SITE=Lax
```

`subtrack.click` và `api.subtrack.click` khác origin nhưng cùng site, do đó `SameSite=Lax` phù hợp. Cookie vẫn là host-only cookie của `api.subtrack.click`, có `HttpOnly` và `Secure`.

Điền Google và Cloudinary:

```env
GOOGLE_CLIENT_ID=<google-oauth-web-client-id>

CLOUDINARY_CLOUD_NAME=<cloud-name>
CLOUDINARY_API_KEY=<api-key>
CLOUDINARY_API_SECRET=<api-secret>
CLOUDINARY_AVATAR_FOLDER=subtrack/avatars
CLOUDINARY_ARTICLE_FOLDER=subtrack/articles
```

Không commit `.env.production`.

## 9. Tạo JWT key production

Trong `SocialMediaBlogPlatform-be`:

```bash
mkdir -p secrets/jwt
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  -out secrets/jwt/private.pem
openssl rsa -pubout -in secrets/jwt/private.pem \
  -out secrets/jwt/public.pem
chmod 600 secrets/jwt/private.pem
chmod 644 secrets/jwt/public.pem
```

Kiểm tra:

```bash
openssl pkey -in secrets/jwt/private.pem -check -noout
openssl pkey -pubin -in secrets/jwt/public.pem -text -noout > /dev/null
```

Thư mục `secrets/` đã được `.gitignore` bỏ qua. Không dùng development JWT keys trong production.

## 10. Validate và khởi động backend

Đặt biến viết tắt cho hai Compose file:

```bash
export COMPOSE_CMD="docker compose --env-file .env.production -f docker-compose.yml -f docker-compose.production.yml"
```

### 10.1. Validate cấu hình

```bash
$COMPOSE_CMD config --quiet
$COMPOSE_CMD config --services
```

Danh sách production không được có service `postgres`. Nếu thiếu một RDS URL hoặc password, Compose sẽ báo lỗi và không cho deploy.

### 10.2. Build và khởi động

```bash
$COMPOSE_CMD up -d --build
```

Kiểm tra container:

```bash
$COMPOSE_CMD ps
$COMPOSE_CMD logs --tail=200 api-gateway
$COMPOSE_CMD logs --tail=200 user-service
```

Theo dõi toàn bộ log trong lần khởi động đầu:

```bash
$COMPOSE_CMD logs -f
```

Cần xác nhận:

- Các service đăng ký thành công với Eureka.
- Flyway migration thành công trên cả sáu database.
- Không có lỗi `connection refused`, `timeout`, `password authentication failed` hoặc `permission denied for schema public`.
- API Gateway lắng nghe tại `127.0.0.1:8080`.

Kiểm tra từ EC2:

```bash
curl http://127.0.0.1:8080/actuator/health
```

Nếu service khởi động trước Config Server/Eureka và thoát, chạy lại:

```bash
$COMPOSE_CMD up -d
```

## 11. Cấu hình Nginx và HTTPS cho API

### 11.1. Cài Nginx và Certbot

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx
sudo systemctl enable --now nginx
```

Đảm bảo `api.subtrack.click` đã trỏ đúng Elastic IP trước khi xin certificate.

### 11.2. Cấp certificate

Config mẫu production tham chiếu certificate sẵn có, vì vậy lấy certificate trước bằng standalone mode:

```bash
sudo systemctl stop nginx
sudo certbot certonly --standalone -d api.subtrack.click
sudo systemctl start nginx
```

### 11.3. Cài Nginx site

Từ thư mục `SocialMediaBlogPlatform-be`:

```bash
sudo cp deploy/nginx/api.subtrack.click.conf.example \
  /etc/nginx/sites-available/api.subtrack.click
sudo ln -sfn /etc/nginx/sites-available/api.subtrack.click \
  /etc/nginx/sites-enabled/api.subtrack.click
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

Nginx proxy:

- REST API vào `http://127.0.0.1:8080`.
- WebSocket `/ws/**` vào cùng Gateway và truyền header Upgrade.
- HTTP tự redirect sang HTTPS.

Kiểm tra certificate renewal:

```bash
sudo certbot renew --dry-run
```

Kiểm tra public API:

```bash
curl -i https://api.subtrack.click/actuator/health
```

## 12. Deploy frontend lên Vercel

### 12.1. Import project

Trong Vercel:

1. Chọn `Add New Project`.
2. Import repository.
3. Đặt `Root Directory` thành `SocialMediaBlogPlatform-fe`.
4. Framework preset chọn Vite nếu Vercel chưa tự nhận.
5. Build command: `npm run build`.
6. Output directory: `dist`.

File `vercel.json` đã cấu hình SPA fallback để truy cập trực tiếp `/login`, `/register`, `/articles/...` không bị 404.

### 12.2. Environment variables

Trong `Project Settings → Environment Variables`, thêm cho môi trường Production:

```env
VITE_API_BASE_URL=https://api.subtrack.click
VITE_WS_BASE_URL=wss://api.subtrack.click
VITE_GOOGLE_CLIENT_ID=<google-oauth-web-client-id>
```

Biến `VITE_*` được đóng vào bundle tại build-time. Sau khi thay giá trị phải redeploy frontend.

Không đưa database password, Cloudinary secret, JWT key hoặc backend secret lên Vercel.

### 12.3. Custom domain

Trong `Project Settings → Domains`:

1. Thêm `subtrack.click`.
2. Cấu hình DNS theo record Vercel hiển thị.
3. Chờ domain chuyển sang trạng thái Valid.
4. Redeploy production nếu cần.

## 13. Cấu hình Google Login production

Trong Google Cloud Console, dùng OAuth 2.0 Client loại Web application.

Authorized JavaScript origins:

```text
http://localhost:5173
https://subtrack.click
```

Luồng hiện tại dùng Google Identity Services popup và gửi ID token về backend, do đó không cần Authorized redirect URI.

Đảm bảo cùng một Client ID được đặt tại:

```text
Vercel: VITE_GOOGLE_CLIENT_ID
EC2:    GOOGLE_CLIENT_ID
```

Sau khi sửa Google Console có thể cần chờ cấu hình được cập nhật rồi thử lại bằng cửa sổ ẩn danh.

## 14. Checklist kiểm tra sau deploy

### 14.1. DNS và TLS

```bash
nslookup subtrack.click
nslookup api.subtrack.click
curl -I https://subtrack.click
curl -i https://api.subtrack.click/actuator/health
```

### 14.2. CORS

```bash
curl -i -X OPTIONS https://api.subtrack.click/api/v1/auth/login \
  -H "Origin: https://subtrack.click" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"
```

Response phải cho phép origin `https://subtrack.click`, không được trả `Access-Control-Allow-Origin: *` khi dùng credentials.

### 14.3. Authentication

Kiểm tra trên trình duyệt:

- Register/login email hoạt động.
- Google Login mở popup và đăng nhập thành công.
- Refresh session hoạt động sau khi reload trang.
- Cookie `refresh_token` có `HttpOnly`, `Secure`, `SameSite=Lax`.
- Logout xóa/revoke refresh token.

### 14.4. WebSocket và notification

- Đăng nhập bằng hai tài khoản.
- Tài khoản A tương tác với nội dung của tài khoản B.
- Tài khoản B nhận notification realtime.
- DevTools không xuất hiện lỗi kết nối `ws://localhost`.
- Kết nối phải dùng `wss://api.subtrack.click/ws/notifications`.

### 14.5. Database

Kiểm tra log Flyway:

```bash
cd /opt/subtrack/app/SocialMediaBlogPlatform-be
$COMPOSE_CMD logs --tail=300 user-service article-service comment-service \
  interaction-service follower-service notification-service
```

Không service nào được đăng nhập bằng RDS master user.

## 15. Cập nhật phiên bản sau này

Trước một release có migration database quan trọng:

1. Tạo manual snapshot RDS.
2. Đảm bảo working tree trên EC2 không chứa thay đổi thủ công.
3. Pull commit/tag đã được kiểm thử.
4. Build và khởi động lại Compose.
5. Theo dõi Flyway và health endpoint.

Ví dụ:

```bash
cd /opt/subtrack/app
git fetch --all --tags
git checkout <release-tag-or-commit>
cd SocialMediaBlogPlatform-be
export COMPOSE_CMD="docker compose --env-file .env.production -f docker-compose.yml -f docker-compose.production.yml"
$COMPOSE_CMD config --quiet
$COMPOSE_CMD up -d --build
$COMPOSE_CMD ps
$COMPOSE_CMD logs --tail=300
```

Không chạy `docker compose down -v` trong production vì `-v` có thể xóa Kafka/Redis volumes. Không dùng `git reset --hard` khi chưa kiểm tra dữ liệu và thay đổi local.

## 16. Rollback

Rollback application:

1. Checkout release/tag trước đó.
2. Build lại image.
3. Chạy `up -d`.
4. Kiểm tra health và log.

```bash
git checkout <previous-release-tag>
cd SocialMediaBlogPlatform-be
$COMPOSE_CMD up -d --build
```

Lưu ý: rollback code không tự rollback Flyway migration. Nếu migration mới không tương thích ngược, cần quy trình migration riêng hoặc restore RDS snapshot sang instance mới rồi đổi sáu JDBC URL. Không restore đè production khi chưa xác nhận dữ liệu cần giữ.

## 17. Lỗi thường gặp

### `Connection timed out` tới RDS

- RDS không cùng VPC với EC2.
- RDS Security Group chưa cho EC2 Security Group vào port `5432`.
- Endpoint hoặc port sai.
- RDS chưa ở trạng thái Available.

### `password authentication failed`

- Password trong `.env.production` không khớp role đã tạo.
- Service đang dùng nhầm username.
- Password chứa ký tự được nhập sai trong SQL hoặc env file.

### Flyway báo thiếu quyền

- Database owner không đúng role của service.
- Database được tạo bằng master owner thay vì role tương ứng.
- Kiểm tra owner bằng `\l` trong `psql`.

### CORS bị chặn

- `CORS_ALLOWED_ORIGINS` phải chính xác là `https://subtrack.click`.
- Không thêm dấu `/` cuối URL.
- Sau khi sửa `.env.production`, chạy lại `$COMPOSE_CMD up -d`.

### Cookie refresh không được gửi

- API phải chạy HTTPS.
- `REFRESH_TOKEN_COOKIE_SECURE=true`.
- FE request phải dùng `credentials: include`.
- Không test production qua một domain Vercel preview khác site rồi kỳ vọng cookie `SameSite=Lax` hoạt động như custom domain.

### WebSocket không kết nối

- Vercel phải có `VITE_WS_BASE_URL=wss://api.subtrack.click` rồi redeploy.
- Nginx phải truyền `Upgrade` và `Connection` headers.
- Kiểm tra `websocket-service` đã đăng ký Eureka.
- Kiểm tra log API Gateway và WebSocket service.

### Google báo origin không hợp lệ

- Thêm đúng `https://subtrack.click` vào Authorized JavaScript origins.
- Client ID FE và BE phải giống nhau.
- Không thêm path như `/login` vào origin.

## 18. Tài liệu tham khảo chính thức

- Docker Engine trên Ubuntu: https://docs.docker.com/engine/install/ubuntu/
- Docker Compose plugin: https://docs.docker.com/compose/install/linux/
- Vercel cho Vite: https://vercel.com/docs/frameworks/frontend/vite
- Kết nối EC2 với RDS: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/ec2-rds-connect.html
- RDS Security Groups: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Overview.RDSSecurityGroups.html
