# AGENTS.md

Template repo for full-stack web apps: Kotlin/Spring Boot backend serving a GraphQL API (Netflix DGS) to a React/Relay frontend, with Postgres behind jOOQ/Exposed and Flyway migrations. The Gradle build orchestrates everything, including the frontend (via the node-gradle plugin, which downloads its own Node/npm).

## Commands

| Task | Command |
|---|---|
| Run the app (full build, then serves on `localhost:8080`) | `./gradlew bootRun` |
| Frontend watch mode (rebuild + typecheck on change, refresh browser) | `./gradlew watchFrontend` |
| Rebuild Relay artifacts after changing a query/fragment | `./gradlew buildRelay` (or `npm run relay-compiler`) |
| Typecheck frontend | `npm run typecheck` |
| Production frontend bundle (typecheck + Vite build) | `npm run build` |
| Backend tests | `./gradlew test` — requires Docker (Testcontainers spins up `postgres:16-alpine`) |
| Apply DB migrations without starting the app | `./gradlew flywayMigrate` |
| Regenerate jOOQ models after a migration | `./gradlew generateJooq` |

Prerequisite: a `.env` file in the repo root with `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `DB_URL_PREFIX` — `build.gradle.kts` reads it eagerly, so **all Gradle commands fail without it**. `./bin/setup_database` creates the database.

## Layout

- `src/main/kotlin/com/application/` — backend. The build assumes exactly one folder under `src/main/kotlin/com/` (codegen paths are derived from it). Subpackages: `controllers`, `services`, `dao`, `graphql` (DGS data fetchers), `views` (kotlinx.html server-side pages), `config`, `db`.
- `src/main/web-frontend/` — frontend (TypeScript, React 19, Relay, React Router, StyleX via the `sx` prop, shared `@spa-kit/*` packages). Bundled by Vite (`vite.config.ts` at repo root) into `src/main/resources/static/bundles/`. See "How pages get their assets" below.
- `src/main/resources/schema/` — the GraphQL schema, split across multiple `.graphql` files. This is the single source of truth for both codegen pipelines below.
- `src/main/resources/db/migration/` — Flyway migrations, named `V<N>__description.sql` with `N` incrementing.
- `spa-route-definitions/` — Gradle subproject holding the `SpaApplicationDefinition` objects: the single source of truth for every SPA's bundle and routes (see "How pages get their assets"). Separate module so spa-routing codegen doesn't cycle with the root `compileKotlin`.
- `submodules/customgenerator` — separate Gradle project supplying the jOOQ `CustomGeneratorStrategy` (must live outside the main project to be on the codegen classpath).
- `.agent/adr/` — architecture decision records: what was chosen, why, and the trade-offs. Read before proposing a change to the stack; add a numbered ADR when making one.

## How pages get their assets

There is no HTML plugin or Vite manifest: `ReactPage.kt` renders the HTML shell and references build outputs **by fixed name**, so the Vite config pins output filenames (`entryFileNames`/`assetFileNames`) and the two sides must stay in sync.

- **Entries**: each SPA is declared as a `SpaApplicationDefinition` in `spa-route-definitions/` (the single source of truth for both bundles and URLs). Codegen turns those into the Vite input map (`SinglePageApplicationBundles.ts`) and typed client route builders (`src/main/web-frontend/routes/`); the spa-routing Spring Boot starter registers a server GET route per defined route, rendered by `AppSpaHtmlRenderer` via `ReactPage`. Bundles are emitted as `/bundles/<id>.bundle.js`.
- **react is not bundled**: `react`/`react-dom` stay external (via Rolldown's `esmExternalRequirePlugin`, which also converts CommonJS `require("react")` calls inside deps like react-relay into imports) and resolve in the browser through the esm.sh import map that `ReactPage.kt` emits.
- **App-wide CSS**: all compiled StyleX rules plus the global reset (`web-frontend/styles.css`, inlined at build time — it is not imported from TypeScript) are emitted as `/bundles/stylex.generated.css` by the `stylexCssFile` plugin in `vite.config.ts`. `ReactPage.kt` links it on every page. StyleX CSS cannot be split per-page by design.
- **Entry-specific CSS**: if an entry's imports bundle CSS (e.g. GraphiQL's `graphiql/style.css` → `/bundles/graphiql.css`), the entry JS does **not** load it — the SPA's `SinglePageApplicationConfig` must link it by overriding `renderHtml()` with a `ReactPage.customHead { link(...) }` (see `GraphiqlSpaConfig`). CSS of *lazily imported* chunks (e.g. monaco) is injected at runtime by Vite and needs no linking.
- **Adding a client route to the main app**: add a `route(...)` to `AppSpaApplication` in `spa-route-definitions/`, run `./gradlew generateClientRoutes generateBundleEntries` (the `buildFrontend`/`watchFrontend` tasks do this automatically), then reference the generated `AppRoutes.<Name>` entry in `App.tsx`'s route list. The server GET mapping appears automatically.
- **Adding a whole new SPA**: add a `SpaApplicationDefinition` object in `spa-route-definitions/` plus a `SinglePageApplicationConfig` `@Component` — no vite.config or controller changes needed.
- **Route authorization**: `App.tsx` wraps its routes in `withRouteAuthorization` + `spaRoutingResolver` (from `@spa-kit/react-router`), which asks `/__spa/route-decision` before each in-page navigation. Application-level rules are a **deny-by-default gate** (spa-routing 0.2.0): an SPA with no rules 404s on every route, so each `SinglePageApplicationConfig` declares `AllowAll()` as the explicit ungated opt-in. Replace it with real `SpaRouteRule`s (e.g. require-login) to gate pages without client changes.

## The four codegen pipelines

1. **DGS (server)**: Gradle's DGS codegen generates Kotlin types from `src/main/resources/schema/` into `build/generated` (package `com.application.graphql`). Runs as part of the build; not committed.
2. **Relay (client)**: `npm run relay-compiler` runs `spa-kit-compile-relay` (from `@spa-kit/node`), which combines the split schema files into a transient `src/main/resources/relay/schema.graphql`, runs `relay-compiler` against it, then deletes it. Artifacts land in `src/main/web-frontend/__generated__/` and **are committed** — rerun after any GraphQL query/fragment/schema change and commit the result. Relay config lives in the `"relay"` key of `package.json`.
3. **jOOQ (database)**: `./gradlew generateJooq` introspects the live Postgres database and writes Kotlin models to `src/main/kotlin/com/application/db/codegen/` — **committed**. Regenerate after running a new migration.
4. **spa-routing (routes)**: the `io.github.caseymcguire.spa-routing` Gradle plugin reads the `SpaApplicationDefinition`s in `spa-route-definitions/` and generates the Vite input map (`SinglePageApplicationBundles.ts`, **committed**), typed TS route builders (`src/main/web-frontend/routes/`, **committed** — kept outside `__generated__` because the Relay compiler deletes unexpected files there), and typed Kotlin route objects (`build/generated`, not committed). `buildFrontend`/`watchFrontend` regenerate the first two automatically.

Never hand-edit generated code (`__generated__/`, `db/codegen/`, `routes/`, `SinglePageApplicationBundles.ts`).

## Workflow conventions

- Commit directly to `master` and push; do not create feature branches or PRs.
- After schema changes, keep all three codegen outputs in sync: migration → `flywayMigrate` → `generateJooq`; GraphQL schema edit → server rebuild picks up DGS types → `buildRelay` for the client.
- Frontend deps are managed in root `package.json`; the checked-in `package-lock.json` matters because Gradle runs `npm install` during `bootRun` builds.
