# Full Stack Spring Boot/React/GraphQL Template

This is a project template I use for creating new web applications. It uses the following technologies
- [Kotlin](https://kotlinlang.org/) as the server-side language of choice
- [Spring Boot](https://spring.io/projects/spring-boot) as the web application framework 
- [kotlinx.html](https://github.com/kotlin/kotlinx.html) for server-side HTML rendering.
- [Postgres](https://www.postgresql.org/) as the database
- [Jooq](https://www.jooq.org/) for creating typesafe SQL queries.
- [Flyway](https://flywaydb.org/) for handling database migrations.
- [GraphQL](https://graphql.org/) as the API query language (using [Netflix DGS](https://netflix.github.io/dgs/))
- [TypeScript](https://www.typescriptlang.org/) as the client-side language of choice
- [React](https://react.dev/) as the UI rendering library.
- [Relay](https://relay.dev/) as the client-side data fetching API.
- [Stylex](https://stylexjs.com/docs/learn/) for client-side styling
- [React Router](https://reactrouter.com/en/main) as the client-side routing framework
- [Vite](https://vite.dev/) for bundling the client-side code
- spa-routing (Kotlin/Gradle/Spring) and `@spa-kit/*` (npm) — my shared libraries for wiring single-page apps into a Spring backend (see below)

## Setup (for Mac)

### Setup database

1) Install [Postgres](https://www.postgresql.org/download/).
2) Create a `.env` file in the project root directory (use `.env.example` as an example)
3) Set the `DB_USER`, `DB_PASSWORD`, and `DB_NAME` variables in your `.env` file as your database username, password, name, respectively.
4) Run `./bin/setup_database` in the root of the project.
5) (Optional) The variable `DB_URL_PREFIX` is set to default Postgres database URL is `jdbc:postgresql://localhost:5432/` but it can be changed. The application assumes that the URL to connect to the database will be `DB_URL_PREFIX` concatenated with `DB_NAME`, where `DB_NAME` is the name of the database specified above (you can read more about connecting to a Postgres database [here](https://www.postgresql.org/docs/6.4/jdbc19100.htm#:~:text=Defaults%20to%20%22localhost%22.)). For example, if your `DB_NAME` variable is `test_db`, then the URL will be assumed to be `jdbc:postgresql://localhost:5432/test_db`.

- For example, suppose our user was named `test_user`, our password `test_password`, and `DB_NAME` was `test_database`, then our `.env` would something like this:
```
DB_USER=test_user
DB_PASSWORD=test_password
DB_NAME=test_database
DB_URL_PREFIX=jdbc:postgresql://localhost:5432/ # keep the default
```

### How to run
In order to start:
```
./gradlew bootRun
```
and navigate to `localhost:8080` in your web browser. 

This command will: 
1. Compile all server-side code.
2. Run any pending database migrations
3. Install or update `node` and `npm`, if necessary
4. Runs `npm install` to get latest node dependencies defined in `package.json`
5. Runs Vite to compile and bundle client-side code
6. Starts the server on `localhost:8080`.

If you make client-side changes and want to see them without restarting the server, you can run Vite in watch mode. In order to do so, open a new terminal and run the following:
```
./gradlew watchFrontend
```
This will make it so that Vite watches the client-side directories for changes and automatically compiles and bundles them into the `build` directory. Then, you can just refresh the page.

---
Whenever you change a client-side GraphQL query supported by Relay, you must rebuild the Relay models. In order to do so, run the following: 

```
./gradlew buildRelay
```
---
# Changing the database
### Adding a database migration

In order to change the database, you must add a new migration. Since we use Flyway for migrations, you can read about how to structure and run repeatable versus versioned migrations [here](https://documentation.red-gate.com/flyway/flyway-cli-and-api/concepts/migrations). 

For simplicity, we'll assume we want to run a versioned migration. From the Flyway link above:

> Versioned migrations have a version, a description and a checksum. The version must be unique. The description is purely informative for you to be able to remember what each migration does. The checksum is there to detect accidental changes. Versioned migrations are the most common type of migration. They are applied in order exactly once.
>
> Each versioned migration must be assigned a unique version. Any version is valid as long as it conforms to the usual dotted notation. For most cases a simple increasing integer should be all you need[...]
>
>Versioned migrations are applied in the order of their versions. Versions are sorted numerically as you would normally expect.

That is, the file name of the SQL file should be `<VERSION>__<short_description>.sql`. Since migrations are run in the sorted numerical order of their filenames, you should look at the version number of the last migration and increment it by 1. For example, if the first migration was named `V1__initial_setup.sql`, the second migration would be something like `V2__add_new_column.sql` and the third one could be `V3__add_second_table.sql`. 


For this project, migrations are stored in `src/main/resources/db/migration` but this can be configured.

### Running a database migration
There are two ways to run all pending migrations:

1. Start the application. 
    - By default, Spring automatically runs Flyway migrations on application startup (see [here](https://docs.spring.io/spring-boot/docs/2.0.0.M5/reference/html/howto-database-initialization.html#howto-execute-flyway-database-migrations-on-startup))
2. Run `./gradlew flywayMigrate` from the application root.

Once all pending migrations are run, you can regenerate the Jooq models for interacting with your tables in a typesafe way on the server.
```
./gradlew generateJooq
```

## How spa-routing and spa-kit work together

The client router, the server's GET mappings, the bundler's entry points, and per-page auth checks
all describe the same set of routes — and drift apart when maintained by hand. Two shared libraries
keep them in sync:

- **spa-routing** (Kotlin) — routes are defined once, as `SpaApplicationDefinition`s in
  `spa-route-definitions/`. Its Gradle plugin generates the Vite entry map and typed route builders
  for both languages, and its Spring Boot starter serves a GET mapping per route (so deep links and
  reloads just work) plus `/__spa/route-decision` for evaluating server-declared route rules.
- **spa-kit** (`@spa-kit/*` on npm) — the client runtime. `App.tsx` builds its react-router routes
  from the generated builders, and `@spa-kit/react-router` checks each navigation against
  `/__spa/route-decision`, so rules like "require login" are declared once on the server and
  enforced on direct loads and in-page navigations alike.

Adding a route is one edit to the route definition; the codegen and starter keep everything else in
step. See AGENTS.md for the mechanics.

