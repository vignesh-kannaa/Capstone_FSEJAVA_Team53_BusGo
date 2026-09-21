# BusGo

Bus ticket booking web app.
_Allocation 1_: project scaffolding for both tiers, the H2/PostgreSQL setup, and the full
authentication flow (register / login / logout) on the API and in the React UI.

```
Capstone_FSEJAVA_Team53_BusGo/
  busgo-backend/    Spring Boot 3.5, Java 21, Maven, Spring Data JPA, H2 (dev) / PostgreSQL (prod)
  busgo-frontend/   React 18 + TypeScript + Vite, React Router, Context API
```

## Run it (order matters)

Prerequisites: JDK 21, Maven 3.9+, Node 20+.

```bash
# 1. Backend (H2 in-memory DB, admin user is seeded automatically)
cd busgo-backend
mvn spring-boot:run            # http://localhost:8080

# 2. Frontend
cd busgo-frontend
npm install
npm run dev                    # http://localhost:5173  (proxies /api -> :8080)
```

| What       | URL                                                                                                                                                          |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| App        | http://localhost:5173                                                                                                                                        |
| Swagger UI | http://localhost:8080/swagger-ui.html (use **Authorize**, paste the login token)                                                                             |
| H2 console | http://localhost:8080/h2-console - JDBC URL `jdbc:h2:mem:busgo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`, user `sa`, empty password |

**Seeded admin (dev only):** `admin@busgo.com` / `Admin@123` (`isAdmin = true`).
Override with env vars `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

### Profiles

| Profile         | DB                                          | Notes                                                                                                                                          |
| --------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| `dev` (default) | H2 in-memory, PostgreSQL compatibility mode | H2 console on, dev JWT secret                                                                                                                  |
| `prod`          | PostgreSQL                                  | `SPRING_PROFILES_ACTIVE=prod` and env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (>= 32 chars, app refuses to start without it) |

### Tests

```bash
cd busgo-backend  && mvn test        # JUnit 5 + Mockito; JaCoCo report: target/site/jacoco/index.html
cd busgo-frontend && npm test        # Vitest + Testing Library
cd busgo-frontend && npm run lint && npm run build
```

## Auth design decision: stateless JWT (Bearer token)

Chosen because the UI is a separate React SPA calling a REST API, and it keeps Swagger/Postman testing trivial
(just an `Authorization` header). No server session, no CSRF surface (no cookies).

| Trade-off                                                       | How it is handled                                                                                                                                                     |
| --------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Token stored in `localStorage` is readable by injected JS (XSS) | React escapes output by default; token lifetime is 60 min. An HttpOnly cookie would be the hardening step.                                                            |
| A JWT cannot be "deleted" on logout                             | Each token has a unique `jti`; logout puts it in an in-memory blacklist until it expires. Resets on server restart - move to Redis/DB for multi-instance deployments. |
| `isAdmin` in a token could go stale                             | The server **never trusts the claim**: the user is re-loaded from the DB on every request, so role changes and deleted users take effect immediately.                 |
| Passwords                                                       | BCrypt. Length limited to 8-72 chars (BCrypt ignores anything past 72 bytes).                                                                                         |

## API contract (auth)

| Method | Path                 | Auth   | Request                   | Success                                                                                     |
| ------ | -------------------- | ------ | ------------------------- | ------------------------------------------------------------------------------------------- |
| POST   | `/api/auth/register` | public | `{email, password, name}` | `201 {id, email, name, isAdmin}`                                                            |
| POST   | `/api/auth/login`    | public | `{email, password}`       | `200 {token, user:{id,email,name,isAdmin}}`                                                 |
| POST   | `/api/auth/logout`   | Bearer | -                         | `204`                                                                                       |
| GET    | `/api/auth/me`       | Bearer | -                         | `200 {id,email,name,isAdmin}` (extra endpoint: lets the UI restore a session after refresh) |

Login returns `user` next to `token` (the project doc only lists `{token}`; this is a superset so the UI needs no second call).

### Error format (every endpoint, including 401/403 from the security filter)

```json
{
  "timestamp": "2026-09-18T12:00:00Z",
  "path": "/api/auth/register",
  "error": "EMAIL_ALREADY_EXISTS",
  "message": "An account with this email already exists"
}
```

| Status          | `error` codes                                                                                                                         |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| 400             | `VALIDATION_ERROR` (message lists `field: problem; ...`), `MALFORMED_REQUEST`                                                         |
| 401             | `UNAUTHORIZED` (missing/invalid/expired/revoked token), `INVALID_CREDENTIALS` (wrong email **or** password - same message on purpose) |
| 403             | `FORBIDDEN`                                                                                                                           |
| 404 / 405 / 415 | `NOT_FOUND` / `METHOD_NOT_ALLOWED` / `UNSUPPORTED_MEDIA_TYPE`                                                                         |
| 409             | `EMAIL_ALREADY_EXISTS`, `DATA_CONFLICT`                                                                                               |
| 500             | `INTERNAL_ERROR` (details only in the server log)                                                                                     |

## For the rest of the team - how to add their allocations

**Backend**

- Throw `ApiException.notFound("...")`, `.conflict(code, "...")`, `.badRequest(code, "...")` from services; `GlobalExceptionHandler` formats them.
- Get the logged-in user in a controller: `@AuthenticationPrincipal AppUserDetails currentUser` -> `currentUser.getId()` (UUID).
- URL rules live in `SecurityConfig`: `GET /api/schedules/**` is public; `/api/buses/**` and non-GET `/api/schedules/**` need `ROLE_ADMIN`; everything else needs a valid token. Extra checks: `@PreAuthorize("hasRole('ADMIN')")`.
- Entities use `UUID` ids (`@GeneratedValue(strategy = GenerationType.UUID)`), like the `users` table. Table is `users` because `user` is reserved.
- Add seed data as an extra `ApplicationRunner` with `@Order` greater than `DataSeeder` (see `config/DataSeeder.java`). Note `gen_random_uuid()` from the doc's SQL is not portable to H2 - let JPA generate ids.

**Frontend**

- `apiRequest<T>(path, {method, body, auth})` in `src/api/client.ts`: adds the token, parses JSON, throws `ApiError {status, code, message}`. A 401 on an authenticated call logs the user out automatically.
- `useAuth()` -> `{ user, isAuthenticated, isAdmin, isLoading, login, register, logout }`. `useToast()` -> `showToast(message, 'success' | 'error' | 'info')`.
- Routes: add pages in `src/App.tsx` inside the public / `<ProtectedRoute />` / `<ProtectedRoute requireAdmin />` groups. `HomePage`, `MyTripsPage`, `AdminPage` are placeholders to replace.
- Login redirect: a logged-out user sent to `/login` from a protected page returns to it after logging in (`location.state.from`).

## Project layout

```
busgo-backend/src/main/java/com/busgo/
  auth/          AuthController, AuthService, dto/ (Register/Login request, Auth/User response)
  security/      JwtService, JwtAuthenticationFilter, TokenBlacklist, AppUserDetails(+Service), 401/403 JSON handlers
  user/          User entity, UserRepository
  config/        SecurityConfig, OpenApiConfig, DataSeeder
  common/error/  ApiException, ErrorResponse, GlobalExceptionHandler
busgo-frontend/src/
  api/ (client, auth)  context/ (Auth, Toast)  hooks/  components/ (Navbar, ProtectedRoute, AppLayout, FormField)
  pages/ (Login, Register, Home*, MyTrips*, Admin*, NotFound, Forbidden)  utils/validation.ts   (* = placeholder)
```

## Known limitations (Foundation scope)

- Logout blacklist and H2 data are in memory (reset on restart).
- No refresh tokens, password reset, e-mail verification or rate limiting on login.
- `AntPathRequestMatcher` is deprecated in Spring Security 6.5; it is used deliberately (H2 console + MVC ambiguity). Revisit when upgrading.
