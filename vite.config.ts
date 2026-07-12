import fs from "node:fs/promises";
import path from "node:path";

import { transformAsync } from "@babel/core";
import stylexPlugin from "@stylexjs/unplugin";
import react from "@vitejs/plugin-react";
import { esmExternalRequirePlugin } from "rolldown/plugins";
import { defineConfig, type Plugin } from "vite";
import checker from "vite-plugin-checker";

const webFrontend = path.resolve(__dirname, "src/main/web-frontend");

// Rewrites graphql`...` tags into imports of the __generated__ artifacts.
// babel-plugin-relay picks up the "relay" config from package.json.
// (@vitejs/plugin-react v6 dropped its babel option, so this runs the plugin directly.)
function relay(): Plugin {
  return {
    name: "relay",
    enforce: "pre",
    async transform(code, id) {
      if (!/\.tsx?$/.test(id) || id.includes("node_modules") || !code.includes("graphql`")) {
        return null;
      }
      const result = await transformAsync(code, {
        plugins: ["relay"],
        parserOpts: {
          plugins: id.endsWith(".tsx") ? ["jsx", "typescript"] : ["typescript"],
        },
        babelrc: false,
        configFile: false,
        filename: id,
        sourceMaps: true,
      });
      return result?.code ? { code: result.code, map: result.map } : null;
    },
  };
}

// The StyleX plugin appends its CSS by re-emitting the target asset, which gets
// deduped to e.g. "index2.css" because the original still holds "index.css" when
// the copy is named (and Rolldown ignores the plugin's attempt to then drop the
// original from the bundle). Fix it up on disk so ReactPage.kt can link stable names.
function stableCssNames(): Plugin {
  return {
    name: "stable-css-names",
    async writeBundle(options, bundle) {
      const outDir = options.dir;
      if (!outDir) return;
      for (const [fileName, item] of Object.entries(bundle)) {
        if (item.type !== "asset" || !fileName.endsWith(".css")) continue;
        const wanted = item.names[0];
        if (!wanted || wanted === fileName) continue;
        // The deduped copy supersedes any stale original emitted under the wanted name
        await fs.rename(path.join(outDir, fileName), path.join(outDir, wanted));
      }
    },
  };
}

export default defineConfig(({ mode }) => ({
  // Spring Boot serves the emitted files from src/main/resources/static/bundles
  base: "/bundles/",
  plugins: [
    // The production build runs tsc via `npm run build`; this covers watch mode
    // (`npm run watch` passes --mode development)
    mode === "development" && checker({ typescript: true }),
    // Keeps react external (resolved in the browser via the import map emitted by
    // ReactPage.kt) while converting the `require("react")` calls inside CommonJS
    // dependencies like react-relay into imports, which Rolldown otherwise leaves
    // as browser-breaking runtime requires (rolldown.rs/in-depth/bundling-cjs)
    esmExternalRequirePlugin({
      external: ["react", "react/jsx-runtime", "react-dom", "react-dom/client"],
    }),
    relay(),
    react(),
    stylexPlugin.vite({
      useCSSLayers: true,
      treeshakeCompensation: true,
      aliases: {
        "*": path.join(webFrontend, "*"),
      },
      // All StyleX rules are merged into one payload and appended to the index
      // entry's stylesheet, which ReactPage.kt links on every page
      cssInjectionTarget: (fileName: string) => fileName === "index.css",
    }),
    stableCssNames(),
  ],
  resolve: {
    // Resolve absolute imports like "pages/HomePage" via tsconfig's baseUrl
    tsconfigPaths: true,
  },
  worker: {
    // The monaco workers pulled in by GraphiQL code-split, which iife (the
    // default) doesn't support
    format: "es",
  },
  build: {
    // The dev watch script overrides this with --outDir to write straight into
    // the build folder Spring serves from (see package.json)
    outDir: "src/main/resources/static/bundles",
    emptyOutDir: true,
    sourcemap: true,
    rolldownOptions: {
      input: {
        index: path.join(webFrontend, "App.tsx"),
        graphiql: path.join(webFrontend, "pages/GraphiqlPage.tsx"),
      },
      // The react family is externalized by esmExternalRequirePlugin above
      external: ["sanitize-html", "highlight.js"],
      output: {
        // Stable names: the Kotlin views reference <entry>.bundle.js and <entry>.css
        entryFileNames: "[name].bundle.js",
        chunkFileNames: "[name]-[hash].js",
        assetFileNames: (assetInfo) => {
          const name = assetInfo.names?.[0] ?? "";
          return name.endsWith(".css") ? "[name][extname]" : "[name]-[hash][extname]";
        },
      },
    },
  },
}));