# Repository Guidelines

## Project Structure & Module Organization

This is a Java 17 Spring Boot server project using Gradle.

- `src/main/java/com/ssafy/culturepick`: application code.
- `auth`: authentication, JWT, OAuth2, email verification, and token storage.
- `member`: member domain and persistence.
- `global`: shared config, exception handling, utilities, and base entities.
- `src/main/resources`: runtime configuration such as `application.yaml`.
- `src/test/java/com/ssafy/culturepick`: integration and context tests.
- `src/test/resources/application-test.yaml`: H2, test Redis, mail, JWT, and OAuth test configuration.
- `docker-compose.yaml`: local infrastructure support, currently including external services such as Redis.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root.

```bash
./gradlew build
```

Builds the project and runs all tests.

```bash
./gradlew test
```

Runs the full JUnit test suite.

```bash
./gradlew test --tests com.ssafy.culturepick.auth.AuthSignupIntegrationTest
```

Runs a single test class. On Windows, use `.\gradlew.bat` instead of `./gradlew`.

```bash
./gradlew bootRun
```

Starts the application locally using configured environment variables.

## Coding Style & Naming Conventions

Use standard Java conventions: 4-space indentation, `PascalCase` classes, `camelCase` fields and methods, and uppercase enum constants. Keep packages feature-oriented under `auth`, `member`, and `global`.

Prefer constructor injection through Lombok `@RequiredArgsConstructor`. Put business validation in services, persistence operations in repositories, and HTTP concerns in controllers. Use existing `BusinessException` and `ErrorCode` enums for expected failures.

## Testing Guidelines

Tests use JUnit, Spring Boot test support, AssertJ, and MockMvc. Integration tests should be named by feature and behavior, for example `AuthLoginIntegrationTest` and methods like `login_success_whenEmailAndPasswordAreValid`.

Use `@ActiveProfiles("test")` for Spring Boot integration tests so they load `application-test.yaml` instead of production-like environment placeholders. Clean up persisted test data in `@AfterEach`, including Redis-backed token or email verification state.

## Commit & Pull Request Guidelines

Recent commits use a Conventional Commit style with Korean descriptions, for example:

- `feat: 회원가입, 이메일 인증 유효성 검사`
- `test: 인증 회원가입 흐름 통합 테스트 추가`
- `chore: PR 템플릿 생성`

Use `feat`, `fix`, `test`, `refactor`, `docs`, or `chore` prefixes. PRs should include a short summary, test results, linked issue when applicable, and any configuration or migration notes.

## Security & Configuration Tips

Do not commit real secrets. Runtime values such as `DB_URL`, `JWT_SECRET_KEY`, mail credentials, OAuth credentials, and CORS origins are supplied through environment variables. Keep test-only secrets in `application-test.yaml`.
