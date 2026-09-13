# Full Stack Spring Boot/React/GraphQL Template

A template for building web applications with a Kotlin/Spring Boot backend and a React/Relay frontend. It uses GraphQL for the API, Postgres with EntKt and Flyway for persistence, and StyleX for styling.

Gradle builds the backend and frontend together. spa-routing and `@spa-kit/*` connect frontend app entry points and routes to Spring's page serving and access rules.

## Setup

Open this checkout in your coding agent and run:

- **Codex:** `$setup-project`
- **Claude Code:** `/setup-project`

The [setup skill](.agents/skills/setup-project/SKILL.md) asks for your project name and root package, configures local prerequisites and the database, installs dependencies, and starts and verifies the app.

## Documentation

- [Architecture](docs/architecture.md) — how apps, spa-routing, frontend bundles, and the backend fit together.
- [Frontend](docs/frontend.md) — React/Relay, StyleX, routing, and the build.
- [Backend](docs/backend.md) — DGS, EntKt, migrations, security, and testing.
