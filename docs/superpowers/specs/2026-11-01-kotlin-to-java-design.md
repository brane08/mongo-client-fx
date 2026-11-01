# Design: Kotlin → Java migration for mongo-client-fx

**Date:** 2026-11-01  
**Status:** Draft pending approval

## Intent and why

`mongo-client-fx` is an older Kotlin/JavaFX desktop MongoDB client. The goal is to bring it onto Java with current libraries while keeping the same application behavior. Persistence moves from an H2 database (via JetBrains Exposed) to a plain HOCON file under the user’s home directory, with passwords encrypted at rest.

## Goals

- Same app: same screens, flows, and capabilities that exist today (including stubs that remain stubs).
- Pure Java source; remove Kotlin language, stdlib, and Kotlin-only libraries.
- Current stable library versions on a modern LTS JDK.
- Connection settings stored as HOCON in the home directory.
- Passwords encrypted in that file; other fields plaintext.

## Non-goals

- New MongoDB features (query runner beyond the existing `TODO` stub, Atlas extras, etc.).
- UI redesign or FXML layout changes beyond what Java/Guice wiring requires.
- Migrating existing H2 `~/mongo-fx` data into the new file format.
- Master-password / OS keyring unlock UX.
- Automated test suite (none exists today; not required for this migration).
- Publishing / packaging redesign (keep Maven jar + JavaFX plugin approach).

## Anti-goals

- Shipping with plaintext passwords in the config file.
- Leaving Kotlin or Exposed in the dependency tree.
- Breaking saved-connection create / list / load / test flows that already work.

## Constraints

- Existing FXML under `src/main/resources/brane08/fx/mongo/` stays the primary UI definition.
- Package root remains `brane08.fx.mongo`.
- Build remains Maven.
- Config must be a plain file a user can inspect (except encrypted password values).

## Current app (what “same” means)

Small JavaFX client with Guice DI and FXML controllers:

| Area | Behavior to preserve |
|------|----------------------|
| Launch | `MongoApp` opens main window sized ~70% of primary screen, centered |
| Main | Open “Manage Connections” and “Open Connection” modal dialogs |
| Manage Connections | Bind name/servers/db/user/password; Save; Test; Close |
| Open Connection | List saved names; selecting one loads details into bound fields |
| Mongo | `testConnection(...)` opens a sync client and lists collections; `execute()` remains unimplemented |
| DI | Guice modules + `GuiceFXMLLoader` controller factory |

Unused / incomplete pieces today (carry forward as-is, do not expand): incomplete menu refresh in `MainView`, unused converters, `Favorites` Exposed model with no callers, `ApplicationException`.

## Target stack (builder may pin exact patch versions within these lines)

| Concern | Choice | Notes |
|---------|--------|-------|
| Language / JDK | Java 25 | Paired with JavaFX 25; raised from 21 for current OpenJFX lines |
| UI | JavaFX 25.x | controls, graphics, fxml (25.0.5) |
| DI | Guice 7.x | Drop `javax.inject` → use Jakarta inject as Guice 7 requires |
| Mongo | mongodb-driver-sync 5.x | Latest stable |
| Logging | Logback 1.5.x / 1.6.x + SLF4J | Latest stable compatible pair |
| Icons | Ikonli 12.4.x | javafx + fontawesome5 pack |
| Config | Typesafe / Lightbend `config` (HOCON) | Latest stable |
| Crypto | JDK only (`AES/GCM/NoPadding`) | No extra crypto library |
| Persistence | Files under `~/.mongo-client-fx/` | Replaces H2 + Exposed |
| Modules | Keep / refresh `module-info.java` | Remove kotlin/exposed; add config module |

Remove: Kotlin plugin/deps, Exposed, H2, jcenter/ktor Bintray repos.

## Config file design

### Layout

```
~/.mongo-client-fx/
  connections.conf    # HOCON, user-readable
  .key                # 256-bit AES key, created on first use, owner-only perms
```

Directory and `.key` are created lazily on first save (or first read that needs decrypt). If `.key` is missing and the conf has encrypted passwords, load fails with a clear application error.

### HOCON shape

```hocon
connections = [
  {
    name = "prod"
    servers = "cluster.example.mongodb.net"
    username = "appuser"
    password = "ENC:base64(iv||ciphertext||tag)"
    database = "admin"
  }
]
```

- `name` is unique; save of an existing name **overwrites** that entry (builder call: upsert by name; H2 unique index previously rejected duplicates — overwrite is friendlier for a file store).
- No numeric `id` in the file model; drop `idProperty` usage tied to Exposed `EntityID` or leave it unused/zero.
- Password field always written in `ENC:…` form. On read, values with the `ENC:` prefix are decrypted; anything else is treated as legacy/plaintext and re-encrypted on next save (builder call: one-way upgrade path, no H2 import).

### Encryption (approved default)

- Algorithm: AES-256-GCM.
- Key: 32 random bytes in `.key`; create with `Files.createFile` + POSIX `rw-------` where supported; on non-POSIX, best-effort owner-only.
- Wire format after `ENC:`: Base64 of `IV (12 bytes) || ciphertext || 128-bit tag` as produced by JCE GCM.
- AAD optional; builder may bind AAD to connection `name` so ciphertext is name-scoped.

### ConfigService API (behavioral contract)

Preserve the existing call shapes used by view models:

- `List<String> getConnectionNames()`
- `Connection getConnectionData(String name)` — throws / surfaces error if missing
- `void save(String name, String servers, String username, String password, String database)`

`Connection` becomes a plain Java record or immutable class (no Exposed entity). Passwords returned from `getConnectionData` are **decrypted** plaintext for UI binding and Mongo test calls.

Replace file rewrite strategy: read-modify-write the full `connections` list atomically (write temp file in same dir, then move/replace).

## Source layout after migration

```
src/main/java/brane08/fx/mongo/
  MongoApp.java
  MainView.java
  ApplicationException.java
  config/
    Connection.java          # model
    FxAppModule.java
    FxStageModule.java
    GuiceFXMLLoader.java
    RootStage.java           # qualifier
  services/
    ConfigService.java       # HOCON + crypto
    MongoService.java
    SecretBox.java           # or package-private helper for encrypt/decrypt
  viewmodels/
    ConnectViewModel.java
    OpenViewModel.java
  views/
    ConnectionView.java
    OpenView.java
  converters/
    StringBooleanConverter.java

src/main/resources/…        # FXML, logback; drop db/config.sql (H2)
```

Delete all `src/main/kotlin/**`. Remove H2 SQL resource. Keep FXML controller `fx:controller` class names aligned with the Java types above.

## Build / run

- `pom.xml`: Java 25 compiler release; maven-compiler-plugin only (no kotlin-maven-plugin).
- `javafx-maven-plugin` updated; main class `brane08.fx.mongo.MongoApp` (fix any stale `brane.fx.mongo/${main.class}` module/main mismatches while touching the POM).
- `module-info.java` updated for new requires / exports / opens needed by JavaFX FXML and Guice reflection.
- Dead Bintray repositories removed; Maven Central only.

## What’s left to the builder

- Exact dependency patch versions within the lines above.
- Whether `Connection` is a `record` vs class; package for crypto helper.
- Precise Guice 7 / Jakarta inject annotation imports and module-info `requires`.
- POSIX vs fallback file-permission handling details.
- Whether to keep or delete clearly dead code (`Favorites` equivalent, unused converter) — prefer delete if nothing references it.
- Smoke-run verification approach on the builder’s machine (GUI).

## Out of scope reminders

Do not invent a working `MongoService.execute()` implementation. Do not redesign FXML. Do not add a settings UI for encryption.
