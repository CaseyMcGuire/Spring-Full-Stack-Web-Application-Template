# AGENTS.md

Kotlin/Spring Boot + DGS GraphQL, React/Relay, Postgres, EntKt, and Flyway. Gradle builds both server and frontend and downloads Node/npm.

## Task scope

- Implement only what the user asks for, including the minimum supporting changes, required code generation, and focused validation needed for that request.
- Broader project goals and earlier discussions provide context; they do not authorize implementing the next feature. Do not add adjacent functionality, refactor unrelated code, or introduce dependencies for work the user has not requested.
- If the scope is ambiguous, prefer the smallest reasonable interpretation and ask before expanding it. Once the requested change is complete, stop and let the user decide the next step.

## Rules and project context

When first working in this repo, read the [architecture guide](docs/architecture.md) for request flows, layer responsibilities, and code generation.

Before planning or editing, read the rules and usage guide relevant to the task:

- Frontend, including Vite and Relay: [frontend rules](.agents/rules/frontend.md) and [frontend guide](docs/frontend.md).
- Backend, including EntKt, migrations, and GraphQL schemas: [backend rules](.agents/rules/backend.md) and [backend guide](docs/backend.md).
- Tasks involving both, including shared GraphQL schemas, SPA routes, or HTML asset rendering: read both sets.

## Commands and setup

| Task | Command |
|---|---|
| Run app on `localhost:8080` | `./gradlew bootRun` |
| Watch frontend; refresh browser after rebuild | `./gradlew watchFrontend` |
| Generate Relay artifacts | `./gradlew buildRelay` |
| Typecheck frontend | `npm run typecheck` |
| Production frontend build | `npm run build` |
| Backend tests (requires Docker) | `./gradlew test` |
| Apply migrations | `./gradlew flywayMigrate` |
| Generate EntKt code / validate schemas | `./gradlew generateEntkt` / `./gradlew validateEntSchemas` |

All Gradle commands require a root `.env` containing `DB_USER`, `DB_PASSWORD`, `DB_NAME`, and `DB_URL_PREFIX`. Use plain `KEY=value` lines without blanks, comments, quotes, or `=` inside values. For prerequisites and fresh-clone setup, read [setup-project](.agents/skills/setup-project/SKILL.md).

## Workflow conventions

- Commit directly to `master` and push; do not create feature branches or PRs.
- Never hand-edit generated files; see the [code generation map](docs/architecture.md#sources-and-generated-outputs) for regeneration and committed outputs.
- Before changing the stack, read the relevant [ADRs](.agent/adr/README.md); add a numbered ADR when making an architectural decision.
