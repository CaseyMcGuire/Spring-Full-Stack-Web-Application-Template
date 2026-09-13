---
name: setup-project
description: Set up a fresh clone of this Kotlin/Spring Boot, EntKt, React/Relay template for local development, including project naming, root package selection, prerequisites, database configuration, dependencies, and a working app. Use for onboarding or fixing local setup, not dependency upgrades or deployment.
---

# Set Up Project

Bring the requested checkout to a working local app. Run the setup, rather than only listing commands, unless the user asks for instructions.

## Read the checkout

- Work from the requested repo root. Read `AGENTS.md`, the setup section of `README.md`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/gradle-daemon-jvm.properties`, and `bin/setup_database` as needed.
- Take Java, Node/npm, dependency, and test-container versions from the checkout; do not substitute the newest releases. The current build downloads Gradle and Node/npm itself. Java must be installed and discoverable by Gradle.

## Choose the project name

When initializing a new project, use the name supplied in the request or ask the user what the project should be called. Collect this together with the root-package choice when both are missing. Reuse an already customized project's name unless a rename is requested.

Set `rootProject.name` in `settings.gradle.kts` to the chosen name and write the same name to `.idea/.name`, creating that file and its directory if missing. Verify the two names match. The project name is independent of the Kotlin root package.

If IntelliJ is already open, let the user know to reload the Gradle project and reopen the project if its displayed name has not refreshed.

## Choose the root package

Use the root package supplied by the user. When initializing a new project from the template and none was supplied, ask for a package such as `org.acme.product` or whether to keep `com.application`. Reuse an already customized checkout's package unless a rename is requested.

When changing the package, before the first build:

- Move the package directories and update package declarations and imports across `src/main/kotlin`, `src/test/kotlin`, `ent-schema/src/main/kotlin`, and `spa-route-definitions/src/main/kotlin`. Preserve subpackages and keep the Spring Boot entry point at the chosen root for component scanning.
- The template infers `com.<singleFolder>` from `src/main/kotlin/com`. Replace that inference with an explicit Gradle `rootPackage` property and derive directory paths from the full dotted package. Update the Gradle group, both `MainKt` entry-point settings, DGS and EntKt output packages, and spa-routing's definition source directory and generated Kotlin package.
- Update current documentation links, test commands, and agent instructions tied to the old package or folder assumption. Regenerate generated code through the build; do not rename it by hand.

## Configure local prerequisites

- Reuse a suitable installed JDK and existing PostgreSQL service. If prerequisites are absent, install the pinned JDK and a supported PostgreSQL version through official distributions or the machine's package manager, within the available execution permissions. Confirm PostgreSQL accepts connections and `psql` is available before provisioning.
- Docker is needed for the Testcontainers integration tests, not for running against a native PostgreSQL service. An existing Docker installation can also host a dedicated local development database when that fits the user's setup; keep it bound to localhost and retain its data volume.
- Honor an existing `.env` and database. Do not replace credentials, recreate an existing database, or stop unrelated services. If the configured database is nonlocal, establish that it is the intended development target before running startup migrations.

## Create or validate `.env`

All Gradle tasks read `.env` during configuration, including tasks that do not use a database. Create it before invoking Gradle.

The current parser splits every line on `=`. It does not support blank lines, comments, quoting, or values containing `=`; shell expansion is also unsupported. Inspect existing configuration without printing passwords. If the parser has changed in the checkout, follow its actual behavior.

For a missing `.env`, choose simple project-specific role/database names. If the chosen role already exists, reuse its supplied credentials or choose a new role rather than resetting its password. For a new role, use the bundled helper from the repo root:

```sh
python3 .agents/skills/setup-project/scripts/create_env.py \
  --repo-root . --db-user app_user --db-name app_dev --port 5432
```

Replace the example names and port to match the chosen local database. The helper creates a private four-line file with a random password, refuses to overwrite an existing file, and never prints credentials. If Python 3 is unavailable, create the same format with an available cryptographic random generator and restrictive file permissions. The required keys are `DB_USER`, `DB_PASSWORD`, `DB_NAME`, and `DB_URL_PREFIX`; the JDBC prefix must end with `/` because the build appends the database name.

## Provision the database

`bin/setup_database` assumes a local administrator named `postgres`, uses `psql` from PATH, and contains Bash syntax despite its `sh` shebang. It does not honor a custom host or port in `DB_URL_PREFIX`.

- For the helper's default local connection, verify admin access first and run `bash ./bin/setup_database`. Because it sources `.env` and interpolates SQL, use it only with verified plain values such as the names and hex password produced by the bundled helper.
- For a different local admin account or endpoint, use that connection to create only the missing role/database. Quote SQL identifiers and values correctly. Make a new application database owned by its application role; ensure an existing database grants that role the schema permissions Flyway needs. Do not change cluster authentication or create a superuser just to satisfy the helper.
- Verify that the configured application credentials connect to the selected database before booting. Pass passwords through protected files or process-local input/environment, keeping them out of command text and displayed output.

## Install, launch, and verify

From the repo root, install frontend dependencies:

```sh
./gradlew npm_ci
```

The explicit clean install ensures frontend tooling exists before bundling. Respect the checked-in lockfile; setup is not an opportunity to upgrade dependencies. On an already configured checkout, reinstall only if needed.

If the root package changed, run `./gradlew clean compileTestKotlin` to regenerate and compile production and test code under the chosen package. This compilation check does not require Docker.

Then start the app:

```sh
./gradlew bootRun
```

`bootRun` generates EntKt entities, DGS types, routes, and bundle entries; builds the frontend; applies Flyway migrations; and starts the server. Relay artifacts are committed, so a fresh clone does not need a separate Relay compile. Never hand-edit generated output.

- Keep the long-running process observable. Wait for successful startup or an actionable error. If the intended port is occupied, reuse the app when appropriate or choose another port with `--args='--server.port=18080'`; do not kill an unrelated process.
- Verify `/` and `/graphiql` in a browser when available. The home page loads data through Relay; seeing its actual welcome message verifies more than an HTTP 200. Execute a read-only query from the checked-in schema in GraphiQL to check the API, or use an HTTP request with the app's existing CSRF cookie/header flow. Preserve security settings.
- If Docker is running, run `./gradlew test`. If it is unavailable, report that integration tests were not run; this does not prevent verifying the app against local PostgreSQL.
- Finish with the working URL, chosen project name and root package, configuration files created, checks passed or skipped, and how to stop the process you started. Leave the requested development app running. If blocked, report the specific missing prerequisite or error and the remaining step; do not claim setup succeeded.

For subsequent work: `./gradlew watchFrontend` rebuilds and typechecks in a second terminal; refresh the browser. Run `./gradlew buildRelay` after changing GraphQL queries or schema.
