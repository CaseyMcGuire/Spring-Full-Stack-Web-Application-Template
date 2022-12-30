This is a starter project for a single-page web app that uses:
- Kotlin
- Spring Boot
- GraphQL using Netflix DGS
- Relay
- Postgres
- Jooq
- Flyway
- React Router
- React

## Setup (for Mac)

### Setup database

1) Install [Postgres](https://www.postgresql.org/download/).
2) Create a `.env` file in the project root directory (use `.env.example` as an example)
3) Set the `DB_USER`, `DB_PASSWORD`, `DB_NAME`, and `DB_URL` variables in your `.env` file as your database username, password, name, and URL, respectively.
4) Run `./bin/setup_database` in the root of the project. 

### How to run
In order to start:
```
./gradlew bootRun
```

In order to run webpack in development mode:

```
./gradlew webpackDevelopment
```

In order to rebuild relay: 

```
./gradlew buildRelay
```