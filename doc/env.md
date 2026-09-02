# Environment Variables

File `src/main/resources/application.yaml` đang dùng các biến môi trường sau.

## Danh sách biến

| Biến | Dùng ở đâu | Ý nghĩa | Ví dụ |
|---|---|---|---|
| `MySQL_URL` | `spring.datasource.url` | JDBC URL của database MySQL | `jdbc:mysql://localhost:3306/project_pulse?useSSL=false&serverTimezone=Asia/Bangkok` |
| `MySQL_PASSWORD` | `spring.datasource.password` | Mật khẩu của user MySQL | `root12345` |
| `API_KEY` | `spring.ai.openai.api-key` | API key cho AI/OpenAI-compatible endpoint | `sk-proj-example123456789` |
| `CLOUD_NAME` | `app.cloud.name` | Tên cloud storage provider | `cloudinary` |
| `CLOUD_KEY` | `app.cloud.key` | API key của cloud storage | `123456789012345` |
| `CLOUD_SECRET` | `app.cloud.secret` | API secret của cloud storage | `cloud-secret-example` |
| `FRONTEND_DOMAIN` | `app.domain.frontend` | Domain của frontend | `http://localhost:3000` |
| `SECRET_KEY` | `jwt.secretKey` | Secret để ký JWT | `my-super-secret-key-at-least-32-chars` |
| `CLIENT_ID` | `outbound.identity.client-id` | OAuth client id | `1234567890-abc.apps.googleusercontent.com` |
| `GOOGLE_KEY` | `outbound.identity.client-secret` | OAuth client secret | `google-client-secret-example` |
| `URL_REDIRECT` | `outbound.identity.redirect-uri` | Redirect URI sau khi login OAuth | `http://localhost:8080/api/oauth2/callback/google` |

## Gợi ý file `.env`

```env
MySQL_URL=jdbc:mysql://localhost:3306/project_pulse?useSSL=false&serverTimezone=Asia/Bangkok
MySQL_PASSWORD=root12345
API_KEY=sk-proj-example123456789
CLOUD_NAME=cloudinary
CLOUD_KEY=123456789012345
CLOUD_SECRET=cloud-secret-example
FRONTEND_DOMAIN=http://localhost:3000
SECRET_KEY=my-super-secret-key-at-least-32-chars
CLIENT_ID=1234567890-abc.apps.googleusercontent.com
GOOGLE_KEY=google-client-secret-example
URL_REDIRECT=http://localhost:8080/api/oauth2/callback/google
```

## Ghi chú

- `username` của datasource hiện đang hard-code là `root`, không nằm trong biến môi trường.
- `jwt.expirationTime` hiện đang cấu hình cố định là `3600`, chưa tách ra biến môi trường.
- Các URL và key ví dụ ở trên chỉ là giá trị mẫu, cần thay bằng giá trị thật theo môi trường dev/staging/prod.
