# Architecture guide

Spring Boot serves the HTML shell, static frontend bundles, GraphQL API, and authentication endpoints from the same application. React runs in the browser; Postgres stores application data. Gradle coordinates the JVM modules, code generation, and frontend tools.

For implementation conventions, read the [frontend rules](../.agents/rules/frontend.md) or [backend rules](../.agents/rules/backend.md). The examples below demonstrate specific integrations; older file layouts may differ from the current coding rules.

## Layer responsibilities

| Area | Responsibility and starting point |
|---|---|
| Browser UI | [App.tsx](../src/main/web-frontend/App.tsx) mounts the router and Relay environment. Pages compose components and issue queries. |
| HTTP and GraphQL | [Controllers](../src/main/kotlin/com/application/controllers/) handle form requests and redirects; [DGS fetchers](../src/main/kotlin/com/application/graphql/QueryDataFetcher.kt) resolve schema fields. |
| Application logic | [Services](../src/main/kotlin/com/application/services/UserService.kt) coordinate operations such as registration and password hashing. |
| Persistence | [DAOs](../src/main/kotlin/com/application/dao/UserDao.kt) use the generated EntKt client; entity policies govern access. Flyway owns SQL schema changes. |
| Page delivery | [SPA definitions](../spa-route-definitions/src/main/kotlin/com/application/spa/) declare bundles and URLs. [AppSpaHtmlRenderer](../src/main/kotlin/com/application/config/AppSpaHtmlRenderer.kt) delegates HTML rendering to `ReactPage`. |

`ent-schema/` and `spa-route-definitions/` are separate Gradle modules because their definitions must compile before the root application's generated code. Build wiring and dependency declarations live in [build.gradle.kts](../build.gradle.kts), [settings.gradle.kts](../settings.gradle.kts), [gradle.properties](../gradle.properties), and [package.json](../package.json).

## Walkthrough: home-page query

1. Spring serves the app shell for a route declared in `AppSpaApplication`. The shell loads the app bundle and the browser mounts `App`.
2. [HomePage](../src/main/web-frontend/pages/HomePage.tsx) defines `HomePageQuery` with a Relay `graphql` tag, imports its generated TypeScript type, and reads it with `useLazyLoadQuery`.
3. The environment created in `App.tsx` sends a POST to `/graphql` with the CSRF header. `RelayRoot` provides the environment and a Suspense boundary.
4. [QueryDataFetcher](../src/main/kotlin/com/application/graphql/QueryDataFetcher.kt)'s `@DgsQuery` method resolves `welcomeMessage`, whose contract lives in [the GraphQL schema](../src/main/resources/schema/). Relay supplies the result to the page.

This query returns a constant; it does not call a service or access the database. There is no existing GraphQL user mutation or database-backed GraphQL feature to copy end to end.

## Walkthrough: registration and login

1. [RegisterPage](../src/main/web-frontend/pages/RegisterPage.tsx) submits a native form to `POST /user`, including a hidden CSRF field.
2. [UserController](../src/main/kotlin/com/application/controllers/UserController.kt) binds the form parameters and calls `UserService.createUser`.
3. [UserService](../src/main/kotlin/com/application/services/UserService.kt) checks for an existing email, hashes the password through Spring's `PasswordEncoder`, and calls `UserDao.createUser`.
4. [UserDao](../src/main/kotlin/com/application/dao/UserDao.kt) creates the entity with an anonymous viewer. `UserPolicy` permits creation but does not permit loading the credential entity afterward. The controller redirects to login, or back to registration for `UserAlreadyExistsException`.
5. [LoginPage](../src/main/web-frontend/pages/LoginPage.tsx) posts to Spring Security's `/login`. [UserDetailsServiceImpl](../src/main/kotlin/com/application/services/UserDetailsServiceImpl.kt) loads credentials through the DAO for password verification.

These are form submissions and redirects, independent of Relay. See the [backend guide](backend.md#authentication-and-authorization) for the security boundaries.

## Sources and generated outputs

| Source to edit | Generation | Outputs and ownership |
|---|---|---|
| `src/main/resources/schema/*.graphql` | Server compilation runs DGS codegen | JVM GraphQL types under `build/generated/`; uncommitted |
| GraphQL schema plus frontend queries/fragments | `./gradlew buildRelay` | `src/main/web-frontend/__generated__/`; committed |
| `ent-schema/` entity definitions | `./gradlew generateEntkt`, also before backend compilation | `build/generated/entkt/`; uncommitted |
| `spa-route-definitions/` | `./gradlew generateClientRoutes generateBundleEntries`; server route generation runs before backend compilation | `src/main/web-frontend/routes/` and `SinglePageApplicationBundles.ts` committed; Kotlin routes under `build/generated/` uncommitted |

Flyway migrations in `src/main/resources/db/migration/` are handwritten SQL. Storage changes require both the entity definition and a migration; EntKt generation does not apply database changes. Relay temporarily combines the split GraphQL schema into `src/main/resources/relay/schema.graphql` and deletes that file after compilation. Client route outputs stay outside Relay's artifact directory because Relay removes unexpected files there.

## Choosing validation

| Change | Relevant check |
|---|---|
| React or TypeScript | `npm run typecheck`; `npm run build` for bundle or styling changes |
| GraphQL schema or operation | `./gradlew buildRelay`, frontend typecheck, and server compilation for schema changes |
| Entity definition | `./gradlew validateEntSchemas` and backend compilation |
| Persistence or registration behavior | `./gradlew test --tests com.application.UserDaoIntegrationTest` with Docker running |
| Routes or HTML assets | Regenerate routes, build frontend, and check direct loads plus in-page navigation in the running app |

`bootRun` installs frontend dependencies and builds bundles before starting Spring. A plain backend compilation or test run does not perform the production frontend build. Setup prerequisites and `.env` handling are covered by the [setup skill](../.agents/skills/setup-project/SKILL.md); architectural rationale lives in the [ADRs](../.agent/adr/README.md).
