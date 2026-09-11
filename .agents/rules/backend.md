# Backend rules

- Database changes: update `ent-schema/`, add the next `V<N>__description.sql` under `src/main/resources/db/migration/`, run `./gradlew flywayMigrate`, then rebuild. Flyway owns physical schema changes; keep EntKt automatic DDL disabled.
- GraphQL schema lives in `src/main/resources/schema/`. After schema changes, rebuild the server for DGS types, run `./gradlew buildRelay`, and commit the client artifacts.
- DGS, EntKt, and Kotlin route outputs under `build/generated/` are not committed.
- Keep exactly one application directory under `src/main/kotlin/com/`; codegen derives package paths from it.
- Public registration uses `UserPolicy`'s create permission and an anonymous viewer, without loading credentials. Ordinary viewers cannot read, update, or delete credential entities. Keep the privacy bypass confined to `UserDao.findByEmail` for authentication; never reuse it for mutations or API queries.
- Use EntKt `withTransaction { tx -> ... }` and the supplied client for multi-operation transactions. Its default Postgres driver does not join Spring `@Transactional`.
- SPA authorization denies access when no rules are declared; `AllowAll()` explicitly permits public access.
