# AGENTS.md

Template repo for full-stack web apps: Kotlin/Spring Boot backend serving a GraphQL API (Netflix DGS) to a React/Relay frontend, with Postgres behind jOOQ/Exposed and Flyway migrations. The Gradle build orchestrates everything, including the frontend (via the node-gradle plugin, which downloads its own Node/npm).

## Commands

| Task | Command |
|---|---|
| Run the app (full build, then serves on `localhost:8080`) | `./gradlew bootRun` |
| Frontend watch mode (rebuild on change, refresh browser) | `./gradlew webpackDevelopment` |
| Rebuild Relay artifacts after changing a query/fragment | `./gradlew buildRelay` (or `npm run relay-compiler`) |
| Typecheck frontend | `npm run typecheck` |
| Production frontend bundle (includes typecheck) | `npm run webpack` |
| Backend tests | `./gradlew test` — requires Docker (Testcontainers spins up `postgres:16-alpine`) |
| Apply DB migrations without starting the app | `./gradlew flywayMigrate` |
| Regenerate jOOQ models after a migration | `./gradlew generateJooq` |

Prerequisite: a `.env` file in the repo root with `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `DB_URL_PREFIX` — `build.gradle.kts` reads it eagerly, so **all Gradle commands fail without it**. `./bin/setup_database` creates the database.

## Layout

- `src/main/kotlin/com/application/` — backend. The build assumes exactly one folder under `src/main/kotlin/com/` (codegen paths are derived from it). Subpackages: `controllers`, `services`, `dao`, `graphql` (DGS data fetchers), `views` (kotlinx.html server-side pages), `config`, `db`.
- `src/main/web-frontend/` — frontend (TypeScript, React 19, Relay, React Router, StyleX via the `sx` prop, shared `@spa-kit/*` packages). Bundled by webpack (configs at repo root) into `src/main/resources/static/bundles/`.
- `src/main/resources/schema/` — the GraphQL schema, split across multiple `.graphql` files. This is the single source of truth for both codegen pipelines below.
- `src/main/resources/db/migration/` — Flyway migrations, named `V<N>__description.sql` with `N` incrementing.
- `submodules/customgenerator` — separate Gradle project supplying the jOOQ `CustomGeneratorStrategy` (must live outside the main project to be on the codegen classpath).

## The three codegen pipelines

1. **DGS (server)**: Gradle's DGS codegen generates Kotlin types from `src/main/resources/schema/` into `build/generated` (package `com.application.graphql`). Runs as part of the build; not committed.
2. **Relay (client)**: `npm run relay-compiler` runs `spa-kit-compile-relay` (from `@spa-kit/node`), which combines the split schema files into a transient `src/main/resources/relay/schema.graphql`, runs `relay-compiler` against it, then deletes it. Artifacts land in `src/main/web-frontend/__generated__/` and **are committed** — rerun after any GraphQL query/fragment/schema change and commit the result. Relay config lives in the `"relay"` key of `package.json`.
3. **jOOQ (database)**: `./gradlew generateJooq` introspects the live Postgres database and writes Kotlin models to `src/main/kotlin/com/application/db/codegen/` — **committed**. Regenerate after running a new migration.

Never hand-edit generated code (`__generated__/`, `db/codegen/`).

## Workflow conventions

- Commit directly to `master` and push; do not create feature branches or PRs.
- After schema changes, keep all three codegen outputs in sync: migration → `flywayMigrate` → `generateJooq`; GraphQL schema edit → server rebuild picks up DGS types → `buildRelay` for the client.
- Frontend deps are managed in root `package.json`; the checked-in `package-lock.json` matters because Gradle runs `npm install` during `bootRun` builds.
