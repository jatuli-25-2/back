# Backend Service

Spring Boot backend for the P-Project application.

## Tech Stack
- Java 17
- Spring Boot 3.5.6
- Spring Web, Spring Data JPA
- Spring Security + OAuth2 Client
- MariaDB
- JWT (jjwt)
- Springdoc OpenAPI

## Project Structure
- `src/main/java/com/p_project`
  - `admin` Admin dashboards and statistics
  - `AI` AI gateway client and DTOs
  - `book` Book report flow
  - `calendar` Calendar summaries and daily writing list
  - `config` Spring config (Security, CORS, Swagger, RestTemplate, file)
  - `customUser` UserDetails integration
  - `diary` Diary stats
  - `email` Email verification
  - `friend` Friend requests and access control
  - `home` Home summary
  - `jwt` JWT filter and utilities
  - `message` Writing session messages
  - `mypage` Profile + account updates
  - `oauth2` OAuth2 success handling
  - `profile` Profile image storage
  - `sociaLogin` OAuth2 provider mapping
  - `user` User registration/login
  - `writing` Writing session flow
- `src/main/resources`
  - `application.properties`
  - `application-oauth.properties`
  - `logback-spring.xml`

## Core Flows
1. **Auth**
   - Local login: `POST /api/users/login`
   - OAuth2 login: `/oauth2/authorization/{provider}` (Google/Naver/Kakao)
   - JWT filter validates `Authorization: Bearer <token>` and optionally refreshes via `X-Refresh-Token`
2. **Writing**
   - Start -> Answer -> Finalize -> Title/Feedback
   - AI server is called via `AiService` (default `http://127.0.0.1:61299`)
3. **Calendar**
   - Monthly summary and daily writing list
4. **Friends**
   - Requests, accept, and friend calendar access
5. **MyPage**
   - Profile image upload and account updates

## Configuration
Set the following environment variables before running:
- `MARIA_DB_URL`
- `MARIA_DB_ID`
- `MARIA_DB_PWD`
- `GOOGLE_SMTP`
- `UPLOAD_DIR`
- `BASE_URL`
- OAuth2 provider variables in `application-oauth.properties`:
  - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`
  - `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `NAVER_REDIRECT_URI`
  - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI`

## Run
```bash
./gradlew bootRun
```

## API Docs
- Swagger UI: `/swagger-ui.html`
- OpenAPI: `/api-docs`

## Notes
- Database schema uses `users`, `writing_sessions`, `messages`, `friends`, `profile`, `social_identities`.
- File uploads are stored under `file.upload-dir` and exposed via `file.base-url`.
