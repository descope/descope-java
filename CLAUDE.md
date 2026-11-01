# CLAUDE.md

Guidance for Claude Code and other contributors. `README.md` is the user documentation. Copy the closest existing endpoint: this file lists only what the code doesn't show.

## Commands

- Build and run all tests: `mvn -B -ntp package`. One test class: `mvn -B -ntp -Dtest='TenantServiceImplTest' test`.
- Lint: `mvn -B -ntp verify -DskipTests -Dgpg.skip`. Checkstyle (`checkstyle.xml`, Google style, 2-space indent, 120-column lines) runs in `verify`, not in `package`, so `package` passing doesn't mean lint passes.
- Live tests (`testFunctional*`, `FGALiveTest`) call a real project and need `DESCOPE_PROJECT_ID` and `DESCOPE_MANAGEMENT_KEY`. CI skips them: `mvn -B -ntp package '-Dtest=*,!%regex[#testFunctional.*],!FGALiveTest'`.

## Checks that only fail in CI or review

- The source level is Java 8 (`maven.compiler.source` 1.8); newer language features break the build.
- CI's Checkstyle job also checks the lines you change in test files; the local lint doesn't check tests, so follow `checkstyle.xml` in new tests too.
- Every public method needs a usage snippet in the matching `README.md` section. A new service also needs a field in `model/mgmt/ManagementServices.java`, wiring in `ManagementServiceBuilder` (auth: `AuthenticationServiceBuilder`), and an entry in the README function index.

## Rules

- New optional parameters on an existing method: add an overload that keeps the existing signature and delegates with `null`, so existing callers keep compiling.
- Auth impls extend `AuthenticationServiceImpl`, whose `getApiProxy` appends the optional auth management key (`authManagementKey` / `DESCOPE_AUTH_MANAGEMENT_KEY`). It lets callers use auth methods whose public access is disabled. New auth methods use `getApiProxy`; never build the `Authorization` header yourself.
- Throw the existing types in `exception/`; don't add new exception types for single endpoints.
- PR titles and commits use Conventional Commits (`feat(mgmt): ...`, `fix(auth): ...`); release-please builds `CHANGELOG.md`, so never edit it by hand.
