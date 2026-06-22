# Changelog

## [1.2.0](https://github.com/descope/descope-java/compare/java-sdk-1.1.0...java-sdk-1.2.0) (2026-06-22)


### Features

* add ssoId support to loadSettings and add loadAllSettings for multi-SSO tenants ([#333](https://github.com/descope/descope-java/issues/333)) ([cdd8db2](https://github.com/descope/descope-java/commit/cdd8db2e1c4ba5a8c1e52fda1a65268bf7494609))
* **http:** also retry on transient status code 520 ([#337](https://github.com/descope/descope-java/issues/337)) ([57a26b0](https://github.com/descope/descope-java/commit/57a26b02f67a8be592bfe9c49a157d87a6098ad2))


### Bug Fixes

* **deps:** update dependency com.fasterxml.jackson.core:jackson-databind to v2.21.3 ([#329](https://github.com/descope/descope-java/issues/329)) ([993b971](https://github.com/descope/descope-java/commit/993b97148eab7ecd215d6db2781880b739719257))
* **deps:** update dependency commons-io:commons-io to v2.22.0 ([#326](https://github.com/descope/descope-java/issues/326)) ([f890791](https://github.com/descope/descope-java/commit/f8907912532dd2f3fa2976f5f3a856d26bdbc84a))
* **deps:** update dependency org.apache.httpcomponents.client5:httpclient5 to v5.6.1 [security] ([#323](https://github.com/descope/descope-java/issues/323)) ([3e2bd77](https://github.com/descope/descope-java/commit/3e2bd77df8a2ad727cabc36c97ab23c908b808c5))
* **deps:** update dependency org.projectlombok:lombok to v1.18.46 ([#327](https://github.com/descope/descope-java/issues/327)) ([23821a5](https://github.com/descope/descope-java/commit/23821a5b89cec12964084db280243c532eaa306d))
* **deps:** update dependency org.slf4j:slf4j-api to v2.0.18 ([#332](https://github.com/descope/descope-java/issues/332)) ([4905839](https://github.com/descope/descope-java/commit/4905839fd69e61f872b0bdf2f21cd0d5c73bf0fa))
* **deps:** update jackson monorepo to v2.21.4 ([#335](https://github.com/descope/descope-java/issues/335)) ([21fb6a5](https://github.com/descope/descope-java/commit/21fb6a50547185ffb7c95a18193233f2461e82ec))
* **deps:** update jackson monorepo to v2.22.0 ([#336](https://github.com/descope/descope-java/issues/336)) ([a3ab40d](https://github.com/descope/descope-java/commit/a3ab40d0161a9f90657fdec9e1caf063ca7e5f21))

## [1.1.0](https://github.com/descope/descope-java/compare/java-sdk-1.0.65...java-sdk-1.1.0) (2026-04-21)


### Features

* add IDPResponse to AuthenticationInfo for SSO exchange ([#321](https://github.com/descope/descope-java/issues/321)) ([9716b64](https://github.com/descope/descope-java/commit/9716b640151418a427272f159cb36419afb35e0a))

## [1.0.65](https://github.com/descope/descope-java/compare/java-sdk-1.0.64...java-sdk-1.0.65) (2026-04-20)


### Bug Fixes

* **deps:** update dependency org.bouncycastle:bcprov-jdk18on to v1.84 [security] ([#317](https://github.com/descope/descope-java/issues/317)) ([87bee0c](https://github.com/descope/descope-java/commit/87bee0c7d7bc06e833b750c4571f74400ebbe6c4))

## [1.0.64](https://github.com/descope/descope-java/compare/java-sdk-1.0.63...java-sdk-1.0.64) (2026-04-15)


### Features

* add release-please for automated releases ([#306](https://github.com/descope/descope-java/issues/306)) ([43a7bcb](https://github.com/descope/descope-java/commit/43a7bcbf6a6065f55f7ab56a51a732148b17bf5c))


### Bug Fixes

* **deps:** update dependency org.projectlombok:lombok to v1.18.44 ([#309](https://github.com/descope/descope-java/issues/309)) ([22c72d8](https://github.com/descope/descope-java/commit/22c72d87d446c2b245ce7a1f84b8cb92c559f84e))
* **deps:** update jackson monorepo to v2.21.2 ([#312](https://github.com/descope/descope-java/issues/312)) ([748dc76](https://github.com/descope/descope-java/commit/748dc760b7dfff3b3473ab4bb8eaef040c36c987))


### Miscellaneous Chores

* reset release-please state and force 1.0.64 cut ([#313](https://github.com/descope/descope-java/issues/313)) ([9ebaad8](https://github.com/descope/descope-java/commit/9ebaad849086df140097d1ba2783d056fcc7ce7b))
