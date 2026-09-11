# Frontend guide

Frontend source lives in `src/main/web-frontend/`. Use the [frontend rules](../.agents/rules/frontend.md) for component structure and styling conventions; examples here demonstrate framework integration, not every detail of the current coding style.

## App runtime and shared packages

[App.tsx](../src/main/web-frontend/App.tsx) creates the browser router and a single Relay environment at module scope, wraps `RouterProvider` in `RelayRoot`, and mounts the result with `renderComponent`.

| Package | API used by this project |
|---|---|
| `@spa-kit/react` | `renderComponent(<App />)` mounts into the HTML shell's `root` element. |
| `@spa-kit/react-relay` | `createRelayEnvironment` configures the GraphQL network and store; `RelayRoot` supplies the Relay provider and Suspense boundary. |
| `@spa-kit/react-router` | `withRouteAuthorization` wraps route loaders; `spaRoutingResolver` asks the server for navigation decisions using generated route IDs. |
| `@spa-kit/node` | `spa-kit-compile-relay` combines the split schema and invokes the Relay compiler. |

After dependency installation, these packages' `dist/index.d.ts` files under `node_modules/@spa-kit/` describe their installed APIs. Their `dist/index.js` files show behavior when the type signatures are insufficient.

## Relay queries and forms

[HomePage](../src/main/web-frontend/pages/HomePage.tsx) demonstrates a page-owned `graphql` query, an imported generated `HomePageQuery` type, and `useLazyLoadQuery<HomePageQuery>(homePageQuery, {})`. The GraphQL tag is compiled into an artifact import by the Relay Babel transform.

The environment helper defaults to POST `/graphql` with credentials included. `App.tsx` supplies a headers callback using [CsrfUtils](../src/main/web-frontend/utils/CsrfUtils.ts), so each request reads the current CSRF token. `RelayRoot` currently has `fallback={null}`, so suspended content has no visible loading indicator. It supplies a Suspense boundary, not an error boundary.

After schema, query, or fragment edits, run `./gradlew buildRelay` and commit `src/main/web-frontend/__generated__/`. The compiler settings live in root `package.json`. Client route files remain outside that directory because Relay deletes unexpected files there.

There are no current examples of fragment composition, GraphQL mutations, optimistic updates, pagination, or query preloading. The shared environment includes subscription transport support, but the application has no subscription schema or feature. These capabilities are not established application patterns merely because a dependency supports them.

[RegisterPage](../src/main/web-frontend/pages/RegisterPage.tsx) and [LoginPage](../src/main/web-frontend/pages/LoginPage.tsx) use native forms with POST actions and [CsrfToken](../src/main/web-frontend/components/CsrfToken.tsx)'s hidden field. Spring handles their redirects. They are useful form-composition examples, not Relay mutation examples.

## Components and StyleX

Components define styles with `stylex.create()` and apply them through the `sx` prop. [FormField](../src/main/web-frontend/components/FormField.tsx) demonstrates typed props, an optional change callback, and StyleX on form elements. Its existing declaration order predates the component file-order rule; follow the rule for new work.

[stylex-sx.d.ts](../src/main/web-frontend/stylex-sx.d.ts) extends React's DOM attribute types for `sx`, and the StyleX Vite plugin compiles it. Imports such as `components/FormField` resolve from `src/main/web-frontend/` through [tsconfig.json](../tsconfig.json) and Vite's TypeScript path resolution.

## Adding routes and SPAs

1. Add a `route(...)` to [AppSpaApplication](../spa-route-definitions/src/main/kotlin/com/application/spa/AppSpaApplication.kt).
2. Run `./gradlew generateClientRoutes generateBundleEntries` (also run by `buildFrontend` and `watchFrontend`).
3. Use the generated `AppRoutes.<Name>.routeId` and `.path` in [App.tsx](../src/main/web-frontend/App.tsx)'s route list and assign the page component.

The spa-routing starter registers the server GET mapping automatically. To add a separate SPA, define a `SpaApplicationDefinition` in `spa-route-definitions/` and a `SinglePageApplicationConfig` Spring component. The definition supplies its frontend entry and routes; no Vite input or controller changes are needed.

`withRouteAuthorization` and `spaRoutingResolver` check `/__spa/route-decision` before in-page navigation. Declare access rules in the SPA's server configuration. Empty application rules deny every route; `AllowAll()` permits public access. Use appropriate `SpaRouteRule`s to restrict pages.

Use the generated `.routeId` and `.path` in each route object; the resolver reads its `id` to identify the server route. The installed authorization wrapper runs before an existing leaf loader and rejects React Router's route `lazy` option. `App.tsx` currently uses `onError: { type: "allow" }`, allowing navigation if the decision request fails. Server endpoint and entity permissions are separate from this navigation behavior.

## JavaScript and HTML

[ReactPage.kt](../src/main/kotlin/com/application/views/ReactPage.kt) renders the HTML shell with fixed asset paths. [vite.config.ts](../vite.config.ts) pins output filenames; keep both sides aligned when renaming assets.

Route codegen produces `SinglePageApplicationBundles.ts`, the Vite input map. Each bundle is served as `/bundles/<id>.bundle.js`; the starter renders it through `AppSpaHtmlRenderer` and `ReactPage`.

React and React DOM resolve through ReactPage's esm.sh import map, including `react/compiler-runtime`. Keep its version aligned with `package.json`. Rolldown's `esmExternalRequirePlugin` converts CommonJS React requires inside dependencies such as Relay into imports; preserve this when changing bundler configuration.

Keep `babel-plugin-react-compiler` first in the Babel pipeline, before Relay's GraphQL rewrite and the React/StyleX transforms.

## CSS

- App-wide StyleX rules and the global reset at `src/main/web-frontend/styles.css` become `/bundles/stylex.generated.css` through Vite's `stylexCssFile` plugin. ReactPage links it on every page. The reset is inlined at build time; don't import it from TypeScript or split StyleX CSS per page.
- Entry-specific CSS needs an explicit stylesheet link in the SPA configuration's `renderHtml()` using `ReactPage.customHead`. Entry JavaScript does not load it. See [GraphiqlSpaConfig](../src/main/kotlin/com/application/config/GraphiqlSpaConfig.kt) for `/bundles/graphiql.css`.
- Vite injects CSS from lazily imported chunks at runtime; it needs no manual link.

## Build and validation

Frontend dependencies and scripts live in root `package.json`; dependency changes include `package-lock.json`. `npm run typecheck` checks TypeScript, and `npm run build` typechecks before producing bundles under `src/main/resources/static/bundles/`. Gradle's `buildFrontend` also generates client routes and bundle entries first.

With the app running, `./gradlew watchFrontend` rebuilds into `build/resources/main/static/bundles/` and reports type errors. Refresh the browser to load changes. Check both direct URL loads and in-page navigation when changing routes, and inspect rendered styles when changing the CSS pipeline. No frontend test runner is currently configured in `package.json`.
