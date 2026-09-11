# Backend guide

Backend source lives under `src/main/kotlin/com/application/`. Read the [architecture walkthroughs](architecture.md) for the existing request paths and the [backend rules](../.agents/rules/backend.md) for constraints.

## Spring and DGS

Spring discovers `@Controller`, `@Service`, `@Component`, and `@Configuration` classes under the application package. Existing services and DAOs use constructor injection. [DatabaseConfiguration](../src/main/kotlin/com/application/config/DatabaseConfiguration.kt) supplies the shared `EntClient` bean from Spring's `DataSource`; [SecurityConfiguration](../src/main/kotlin/com/application/config/SecurityConfiguration.kt) supplies the password encoder and security filter chain.

The GraphQL contract is split across `src/main/resources/schema/*.graphql`. [QueryDataFetcher](../src/main/kotlin/com/application/graphql/QueryDataFetcher.kt) shows `@DgsComponent` with a `@DgsQuery` method whose name matches the `welcomeMessage` field. Schema edits feed both DGS and Relay generation; defining an EntKt entity does not expose it through GraphQL.

The implemented fetcher returns a constant. There is no established application example for GraphQL mutations, field batching, pagination, or domain-error mapping. The registration controller instead translates `UserAlreadyExistsException` into a redirect. Treat that as the form flow's behavior, not a GraphQL error convention.

## EntKt model and client

[The User schema](../ent-schema/src/main/kotlin/com/application/schema/User.kt) declares `EntSchema("users", clientName = "users")`, a long ID, unique email, and a sensitive password-hash field. Generation produces `com.application.ent.User`, `EntClient`, and typed query/mutation APIs. The schema currently covers `users`; the `posts` table exists only in Flyway SQL.

[UserDao](../src/main/kotlin/com/application/dao/UserDao.kt) demonstrates the APIs in use:

```kotlin
entClient.users.query {
  where(EntUser.email eq email)
}.firstOrNull(credentialLookupContext).getOrThrow()
```

`EntUser` aliases the generated entity to distinguish it from the service's `User` value. The predicate uses a generated field reference. Execution receives an explicit viewer context; `getOrThrow()` surfaces a failed read, while a successful lookup with no match returns null. This particular context is private to the DAO's credential lookup and bypasses privacy for authentication.

Registration uses the normal policy path:

```kotlin
entClient.users.create {
  this.email = email
  this.hashedPassword = hashedPassword
}.save(ViewerContext(Viewer.Anonymous)).getOrThrow()
```

`hashedPassword` has already been encoded by `UserService`. Creation and loading have separate permissions, so registration does not reload the saved credential entity. The service-facing value is constructed from the supplied fields.

For operations requiring a transaction, `entClient.withTransaction { tx -> ... }` supplies an `EntTransactionClient` and returns `TransactionResult<T>`. Use `tx` for operations inside the block and handle the returned result. The configured Postgres driver does not join Spring `@Transactional` scopes. After generation, the exact entity-specific API is inspectable under `build/generated/entkt/com/application/ent/`; those files are generated, not application source.

## Flyway and schema changes

Flyway applies the SQL migrations in [db/migration](../src/main/resources/db/migration/), either at application startup or through `./gradlew flywayMigrate`. [DatabaseConfiguration](../src/main/kotlin/com/application/config/DatabaseConfiguration.kt) disables EntKt automatic DDL.

To change storage, edit the EntKt definition and add the next numbered migration, apply it, then compile the backend. Physical column types and constraints come from the migration: the current string fields map to SQL `VARCHAR(255)` columns. `generateEntkt` and `validateEntSchemas` operate on definitions and do not require a live database. See the [generation map](architecture.md#sources-and-generated-outputs) for output ownership.

## Authentication and authorization

- **Login:** Spring Security handles form login. The field is named `username` even though its value is an email. `UserDetailsServiceImpl` adapts DAO results through `UserDetailsImpl`, and the configured BCrypt encoder verifies passwords.
- **CSRF:** `CookieCsrfTokenRepository` and [CsrfCookieFilter](../src/main/kotlin/com/application/config/CsrfCookieFilter.kt) make the token available as `XSRF-TOKEN`. Browser forms submit `_csrf`; Relay sends `X-XSRF-TOKEN`. The filter resolves the deferred token on the initial page load.
- **Page access:** `SinglePageApplicationConfig` rules govern SPA routes. Both current configs explicitly use `AllowAll()`. The Spring HTTP configuration also currently permits all request paths; having login configured does not make every endpoint require authentication.
- **Entity access:** [UserPolicy](../src/main/kotlin/com/application/db/policies/UserPolicy.kt) permits public creation. Ordinary viewers cannot load, update, or delete credentials. The private `UserDao.findByEmail` bypass is used for internal credential lookup, including the registration duplicate check. Page access decisions do not replace these data-access policies.

## Testing examples

[UserDaoIntegrationTest](../src/test/kotlin/com/application/UserDaoIntegrationTest.kt) boots Spring with `@SpringBootTest` and a Postgres Testcontainer. `@ServiceConnection` supplies the container connection; Flyway prepares its schema. Tests exercise the actual DAO, duplicate-email constraint, password hashing, authentication adapter, and privacy denials. Fixture creation uses EntKt or the DAO; a separately justified test context inspects stored credentials.

Run that class with `./gradlew test --tests com.application.UserDaoIntegrationTest` and Docker running. [GlobalIdUtilTest](../src/test/kotlin/com/application/graphql/GlobalIdUtilTest.kt) is a smaller example of testing a pure utility without a Spring context. The repo currently has no GraphQL integration-test example.
