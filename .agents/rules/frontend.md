# Frontend rules

- Put each React component in its own file by default. Small components that are implementation details of another component may share its file.
- Keep each component's styles in its own `stylex.create()` call, except for shared themes.
- Break components into logical subcomponents with focused responsibilities.
- After imports, order each component file as: styles, extra data structures, props, then component code.

- Frontend dependencies live in root `package.json`; commit corresponding `package-lock.json` changes.
- After GraphQL schema, query, or fragment changes, run `./gradlew buildRelay` and commit `src/main/web-frontend/__generated__/`.
- Routes and bundle entries originate in `spa-route-definitions/`. Run `./gradlew generateClientRoutes generateBundleEntries` after edits; `buildFrontend` and `watchFrontend` also regenerate them. Commit `src/main/web-frontend/routes/` and `SinglePageApplicationBundles.ts`.
- Keep client routes outside `__generated__/` because Relay deletes unexpected files there.
- Keep Vite's fixed output filenames aligned with `ReactPage.kt`, and its React import-map version aligned with `package.json`.
- Read [frontend assets and routing](../../docs/frontend.md) when changing bundles, CSS, or routes.
