---
description: How pages get their JS/CSS assets — the contract between ReactPage.kt and vite.config.ts, and how to add a new page
---

# How pages get their assets

There is no HTML plugin or Vite manifest: `ReactPage.kt` renders the HTML shell and references build outputs **by fixed name**, so the Vite config pins output filenames (`entryFileNames`/`assetFileNames`) and the two sides must stay in sync.

## Entries

Each page is a Vite entry declared in `build.rolldownOptions.input` in `vite.config.ts` (`index` → `App.tsx`, `graphiql` → `GraphiqlPage.tsx`), emitted as `/bundles/<entry>.bundle.js`. A controller serves it with `ReactPage("<entry>", "<title>")`.

## react is not bundled

`react`/`react-dom` stay external — via Rolldown's `esmExternalRequirePlugin`, which also converts CommonJS `require("react")` calls inside dependencies like react-relay into imports — and resolve in the browser through the esm.sh import map that `ReactPage.kt` emits.

## App-wide CSS

All compiled StyleX rules plus the global reset (`web-frontend/styles.css`, inlined at build time — it is not imported from TypeScript) are emitted as `/bundles/stylex.generated.css` by the `stylexCssFile` plugin in `vite.config.ts`. `ReactPage.kt` links it on every page. StyleX CSS cannot be split per-page by design: its atomic classes are deduplicated and ordered globally.

## Entry-specific CSS

If an entry's imports bundle CSS (e.g. GraphiQL's `graphiql/style.css` → `/bundles/graphiql.css`), the entry JS does **not** load it — the page's controller must link it explicitly:

```kotlin
ReactPage("graphiql", "GraphiQL")
  .customHead {
    link(rel = "stylesheet", href = "/bundles/graphiql.css")
  }
  .render()
```

CSS of *lazily imported* chunks (e.g. monaco) is injected at runtime by Vite and needs no linking.

## Adding a page

1. Add the entry to `build.rolldownOptions.input` in `vite.config.ts`.
2. Add a controller method returning `ReactPage("<entry>", "<title>").render()`.
3. Only if the entry imports CSS: link `/bundles/<entry>.css` via `customHead` as above.
