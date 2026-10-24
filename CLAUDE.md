# CLAUDE.md

Guidance for Claude Code and other contributors working in this repository. `README.md` is the user documentation.

## Commands

- Build and run all tests: `mvn -B -ntp package`.
- Lint: `mvn -B -ntp verify -DskipTests -Dgpg.skip`. Checkstyle (`checkstyle.xml`, Google style, 2-space indent, 120-column lines) runs in the `verify` phase, not in `package`, and fails the build on errors.
- One test class: `mvn -B -ntp -Dtest='TenantServiceImplTest' test`.
- Live tests (`testFunctionalFullCycle`, `@RetryingTest`) call a real Descope project and need `DESCOPE_PROJECT_ID` and `DESCOPE_MANAGEMENT_KEY`. Without them they fail locally; unit tests do not need them.
- The source level is Java 8 (`maven.compiler.source` 1.8). Do not use newer language features.

## Adding an endpoint

Paths are under `src/main/java/com/descope/`. For a method on an existing service (management in `sdk/mgmt/`, auth in `sdk/auth/`):

1. `literals/Routes.java`: route constant in `ManagementEndPoints` or `AuthEndPoints`, full path including `/v1/`.
2. `sdk/mgmt/<X>Service.java`: interface method with Javadoc, declaring `throws DescopeException`.
3. `sdk/mgmt/impl/<X>ServiceImpl.java`: implementation.
4. `model/<area>/`: request and response models, in the package layout the area already uses.
5. `src/test/java/com/descope/sdk/mgmt/impl/<X>ServiceImplTest.java`: unit tests.
6. `README.md`: usage snippet in the matching "Manage X" section.

A new service also needs a field in `model/mgmt/ManagementServices.java`, wiring in `sdk/mgmt/impl/ManagementServiceBuilder` (auth: `sdk/auth/impl/AuthenticationServiceBuilder`), and an entry in the README function index.

## Conventions

- Request bodies: a Lombok model (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`), or `CollectionUtils.mapOf(...)` for one or two fields. Jackson omits `null` fields (`Include.NON_NULL`) and ignores unknown response fields.
- Required arguments: `if (StringUtils.isBlank(x)) { throw ServerCommonException.invalidArgument("x"); }`.
- New optional parameters on an existing method: add an overload. Keep the existing signature and have it delegate with `null`, so existing callers keep compiling.
- Credentials:
  - Management impls extend `ManagementsBase` and use `getApiProxy()` (project ID + management key).
  - Auth methods acting for a user call `validateJWT(refreshToken)` first, then `getApiProxy(refreshToken)`.
- Errors: throw the existing types in `exception/` (`ServerCommonException`, `ClientFunctionalException`, ...). Do not add new exception types for single endpoints.

## Tests

Follow the existing `*ServiceImplTest` classes:

- `TestUtils.getClient()` in `@BeforeEach`.
- A mocked `ApiProxy`, returned through `mockStatic(ApiProxyBuilder.class)`.
- Assertions with AssertJ.
- `assertThrows(ServerCommonException.class, ...)` for each required argument.

## Pull requests

PR titles and commits use Conventional Commits (`feat(mgmt): ...`, `fix(auth): ...`); release-please builds `CHANGELOG.md` from them, so never edit it by hand.
