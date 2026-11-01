# Kotlin → Java Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert mongo-client-fx from Kotlin to Java 21 with current libraries, replacing H2/Exposed with an encrypted-password HOCON file under `~/.mongo-client-fx/`.

**Architecture:** Keep the existing JavaFX + Guice + FXML shape. Port controllers/view-models/services to Java. New `ConfigService` reads/writes HOCON via Typesafe Config; `SecretBox` encrypts passwords with AES-256-GCM using a local `.key` file. Drop all Kotlin and Exposed/H2 dependencies.

**Tech Stack:** Java 21, JavaFX 27.x, Guice 7.x (Jakarta inject), mongodb-driver-sync 5.x, Lightbend Config, Logback, Ikonli 12.4.x, JUnit 5 (new unit tests for crypto/config only), Maven.

**Spec:** `docs/superpowers/specs/2026-11-01-kotlin-to-java-design.md`

## Global Constraints

- Package root: `brane08.fx.mongo`
- JDK: Java 21 LTS; Maven build
- Config dir: `~/.mongo-client-fx/` with `connections.conf` + `.key`
- Password wire form: `ENC:` + Base64(`IV(12) || ciphertext || tag`) via `AES/GCM/NoPadding`
- Guice 7 uses `jakarta.inject`, not `javax.inject`
- FXML under `src/main/resources/brane08/fx/mongo/` kept; `fx:controller` names must match Java types
- No H2 migration; no master-password UI; `MongoService.execute()` stays unimplemented
- Upsert connections by `name` on save
- Do not leave Kotlin, Exposed, or H2 in the dependency tree

## Review Focus

- Missing `.key` while `connections.conf` has `ENC:` passwords → `ApplicationException` with a clear message (not a raw crypto stack only)
- Save with an existing `name` → overwrites that entry; other entries unchanged
- Password without `ENC:` prefix on load → returned as plaintext; next save writes `ENC:…`
- `getConnectionData` for unknown name → `ApplicationException`
- Interrupted rewrite of `connections.conf` → use temp file + atomic move so a crash does not leave a truncated final file

---

## File structure

| File | Responsibility |
|------|----------------|
| `pom.xml` | Java 21 Maven; current deps; no Kotlin |
| `src/main/java/module-info.java` | JPMS requires/opens for JavaFX, Guice, config |
| `…/config/Connection.java` | Immutable connection model |
| `…/services/SecretBox.java` | AES-GCM encrypt/decrypt + key file |
| `…/services/ConfigService.java` | HOCON load/save/list |
| `…/services/MongoService.java` | Mongo test connection (execute stub) |
| `…/ApplicationException.java` | Runtime app errors |
| `…/config/FxAppModule.java`, `FxStageModule.java`, `GuiceFXMLLoader.java`, `RootStage.java` | Guice wiring |
| `…/viewmodels/*`, `…/views/*`, `MainView.java`, `MongoApp.java` | UI layer port |
| `src/test/java/…/services/SecretBoxTest.java` | Crypto tests |
| `src/test/java/…/services/ConfigServiceTest.java` | HOCON + upsert + ENC tests |
| Delete | All `src/main/kotlin/**`, `src/main/resources/db/config.sql` |

---

### Task 1: Maven Java foundation + SecretBox

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/brane08/fx/mongo/services/SecretBox.java`
- Create: `src/main/java/brane08/fx/mongo/ApplicationException.java`
- Test: `src/test/java/brane08/fx/mongo/services/SecretBoxTest.java`

**Interfaces:**
- Consumes: none
- Produces:
  - `ApplicationException extends RuntimeException` with `(String message)` and `(String message, Throwable cause)`
  - `SecretBox(Path keyFile)`
  - `String encrypt(String plaintext)` → `ENC:` + Base64(IV‖ciphertext‖tag)
  - `String decrypt(String encoded)` → plaintext; throws `ApplicationException` if key missing/invalid or ciphertext bad
  - Key file: create 32 random bytes on first encrypt if absent; POSIX `rw-------` when supported

- [ ] **Step 1: Rewrite `pom.xml` for Java 21 (no Kotlin)**

Keep `groupId`/`artifactId`/`version`. Set `maven.compiler.release` to `21`, `main.class` to `brane08.fx.mongo.MongoApp`. Dependencies (pin current stable within these lines): JavaFX 27.x controls/graphics/fxml, Guice 7.x, mongodb-driver-sync 5.x, logback-classic 1.5+/1.6+, `com.typesafe:config` latest stable, ikonli-javafx + ikonli-fontawesome5-pack 12.4.x, JUnit Jupiter 5.x (test). Plugins: `maven-compiler-plugin` 3.13+, `maven-surefire-plugin` 3.x, `javafx-maven-plugin` current with mainClass `brane08.fx.mongo.MongoApp`, `maven-assembly-plugin` optional jar-with-dependencies using the same mainClass. Remove kotlin plugin/deps, Exposed, H2, Bintray repos. **Do not delete Kotlin sources yet** (compile will ignore them once source root is `src/main/java` only).

- [ ] **Step 2: Write failing `SecretBoxTest`**

```java
@Test
void roundTrip_encryptDecrypt_returnsOriginal(@TempDir Path dir) { … }

@Test
void decrypt_missingKeyFile_throwsApplicationException(@TempDir Path dir) { … }

@Test
void encrypt_createsKeyFileWithOwnerOnlyPermissionsWhenPosix(@TempDir Path dir) { … }
```

Assertions: round-trip equality; decrypt with absent key after planting an `ENC:` value throws `ApplicationException`; on POSIX, key file permissions equal `rw-------`.

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn -q -Dtest=SecretBoxTest test`  
Expected: FAIL (class missing or compile failure)

- [ ] **Step 4: Implement `ApplicationException` and `SecretBox`**

Algorithm for encrypt: 12-byte random IV; `Cipher.getInstance("AES/GCM/NoPadding")` with `GCMParameterSpec(128, iv)`; output `ENC:` + Base64(iv ‖ doFinal(plaintextUTF8)). Decrypt: require `ENC:` prefix (else return input unchanged — plaintext passthrough lives in ConfigService; SecretBox.decrypt may require `ENC:` and throw otherwise — **decide: SecretBox only handles `ENC:` values; ConfigService strips/detects prefix**). Key load/create as in Interfaces.

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -q -Dtest=SecretBoxTest test`  
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java/brane08/fx/mongo/ApplicationException.java \
  src/main/java/brane08/fx/mongo/services/SecretBox.java \
  src/test/java/brane08/fx/mongo/services/SecretBoxTest.java
git commit -m "$(cat <<'EOF'
feat: add Java 21 Maven foundation and AES-GCM SecretBox

EOF
)"
```

---

### Task 2: Connection model + ConfigService (HOCON)

**Files:**
- Create: `src/main/java/brane08/fx/mongo/config/Connection.java`
- Create: `src/main/java/brane08/fx/mongo/services/ConfigService.java`
- Test: `src/test/java/brane08/fx/mongo/services/ConfigServiceTest.java`

**Interfaces:**
- Consumes: `SecretBox`, `ApplicationException`
- Produces:
  - `record Connection(String name, String servers, String username, String password, String database)` — `password` is plaintext when returned from the service
  - `ConfigService(Path configDir)` — uses `configDir.resolve("connections.conf")` and `configDir.resolve(".key")`; also `ConfigService()` delegating to `Path.of(System.getProperty("user.home"), ".mongo-client-fx")`
  - `List<String> getConnectionNames()`
  - `Connection getConnectionData(String name)`
  - `void save(String name, String servers, String username, String password, String database)`

- [ ] **Step 1: Write failing `ConfigServiceTest`**

```java
@Test
void saveAndLoad_roundTripsDecryptedPassword(@TempDir Path dir) { … }

@Test
void save_sameName_overwritesEntry(@TempDir Path dir) { … }

@Test
void getConnectionData_unknownName_throwsApplicationException(@TempDir Path dir) { … }

@Test
void load_plaintextPassword_returnedAsIs_andResaveEncrypts(@TempDir Path dir) { … }

@Test
void load_encPasswordWithoutKey_throwsApplicationException(@TempDir Path dir) { … }
```

For plaintext test: write a HOCON file with a non-`ENC:` password, assert load returns that string, save again, assert file content contains `ENC:` for that entry’s password. For missing-key test: write conf with `ENC:…` and do not create `.key`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ConfigServiceTest test`  
Expected: FAIL

- [ ] **Step 3: Implement `Connection` and `ConfigService`**

HOCON list `connections = [ { name, servers, username, password, database } ]` via Typesafe Config. On save: load existing list (or empty), upsert by `name`, encrypt password with `SecretBox`, write to a temp file in `configDir` then `Files.move(..., ATOMIC_MOVE)` (fallback to replace if atomic unsupported). Create `configDir` if missing. Decrypt passwords on read when value starts with `ENC:`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=ConfigServiceTest,SecretBoxTest test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/brane08/fx/mongo/config/Connection.java \
  src/main/java/brane08/fx/mongo/services/ConfigService.java \
  src/test/java/brane08/fx/mongo/services/ConfigServiceTest.java
git commit -m "$(cat <<'EOF'
feat: store connections in HOCON with encrypted passwords

EOF
)"
```

---

### Task 3: MongoService

**Files:**
- Create: `src/main/java/brane08/fx/mongo/services/MongoService.java`
- Test: `src/test/java/brane08/fx/mongo/services/MongoServiceTest.java`

**Interfaces:**
- Consumes: `Connection`
- Produces:
  - `String execute()` — throws `UnsupportedOperationException` (preserves unimplemented status)
  - `boolean testConnection(String servers, String username, String password, String database)`
  - `boolean testConnection(Connection connection)` — delegates to the string overload using connection fields
  - Private helper builds `mongodb+srv://user:pass@servers/database?authSource=admin` and uses `MongoClients.create`

- [ ] **Step 1: Write failing test for the stub**

```java
@Test
void execute_throwsUnsupportedOperationException() { … }
```

Do not attempt live Mongo integration tests.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=MongoServiceTest test`  
Expected: FAIL

- [ ] **Step 3: Implement `MongoService`**

Port logic from `src/main/kotlin/brane08/fx/mongo/services/MongoService.kt` (`testConnection` try/catch `MongoException` → false; success → true). Fix the Kotlin bug where `testConnection(Connection)` passed `connection.servers` as username — Java version must pass `connection.username()`.

- [ ] **Step 4: Run tests**

Run: `mvn -q -Dtest=MongoServiceTest,ConfigServiceTest,SecretBoxTest test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/brane08/fx/mongo/services/MongoService.java \
  src/test/java/brane08/fx/mongo/services/MongoServiceTest.java
git commit -m "$(cat <<'EOF'
feat: port MongoService to Java

EOF
)"
```

---

### Task 4: Guice DI + FXML loader

**Files:**
- Create: `src/main/java/brane08/fx/mongo/config/RootStage.java`
- Create: `src/main/java/brane08/fx/mongo/config/GuiceFXMLLoader.java`
- Create: `src/main/java/brane08/fx/mongo/config/FxAppModule.java`
- Create: `src/main/java/brane08/fx/mongo/config/FxStageModule.java`

**Interfaces:**
- Consumes: `ConfigService`, `MongoService`
- Produces:
  - `@Qualifier @Retention(RUNTIME) @Target({FIELD, PARAMETER, METHOD}) @interface RootStage`
  - `GuiceFXMLLoader(Injector injector)` with `Parent load(String relativeFile)` loading `brane08/fx/mongo/` + relativeFile via controller factory `injector::getInstance`
  - `FxAppModule` `@Provides @Singleton` for `MongoService`, `ConfigService` (no-arg home-dir ctor), `GuiceFXMLLoader`
  - `FxStageModule(Stage stage)` binds `@RootStage Stage` to the instance

- [ ] **Step 1: Implement the four DI types**

Use `jakarta.inject` annotations. No unit test required beyond compile in later task; keep modules minimal as in Kotlin `di.kt`.

- [ ] **Step 2: Compile main sources that exist so far**

Run: `mvn -q -DskipTests compile`  
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/brane08/fx/mongo/config/
git commit -m "$(cat <<'EOF'
feat: port Guice modules and FXML loader to Java

EOF
)"
```

---

### Task 5: View models, views, and application entry

**Files:**
- Create: `src/main/java/brane08/fx/mongo/viewmodels/ConnectViewModel.java`
- Create: `src/main/java/brane08/fx/mongo/viewmodels/OpenViewModel.java`
- Create: `src/main/java/brane08/fx/mongo/views/ConnectionView.java`
- Create: `src/main/java/brane08/fx/mongo/views/OpenView.java`
- Create: `src/main/java/brane08/fx/mongo/MainView.java`
- Create: `src/main/java/brane08/fx/mongo/MongoApp.java`
- Omit: Kotlin `Favorites`, unused converter unless an FXML references it (none do — skip)

**Interfaces:**
- Consumes: DI types, `ConfigService`, `MongoService`, `Connection`
- Produces: FXML controller classes matching existing `fx:controller` values; `MongoApp` with `main` + `start` matching Kotlin sizing (`SCALE_FACTOR = 70`)

- [ ] **Step 1: Port view models**

`ConnectViewModel`: JavaFX properties `name`, `connectionString`, `userName`, `password`, `database`, `testConnection` (boolean); methods `loadConnection(String)`, `saveConnection()`, `testConnection()`. Drop Exposed `id` property (or leave unused `SimpleIntegerProperty` at 0 — prefer drop).  
`OpenViewModel`: `ObservableList<String> getNames()`.

- [ ] **Step 2: Port views and `MainView` / `MongoApp`**

Match Kotlin field `fx:id` names and method names referenced from FXML (`saveConnection`, `testConnection`, `close`, `openConnectWindow`, `openConnection`). Use constructor `@Inject` compatible with Guice. Keep incomplete `refreshNames` behavior as in Kotlin (do not expand).

- [ ] **Step 3: Compile**

Run: `mvn -q -DskipTests compile`  
Expected: SUCCESS (Java sources only)

- [ ] **Step 4: Commit**

```bash
git add src/main/java/brane08/fx/mongo/
git commit -m "$(cat <<'EOF'
feat: port JavaFX views, view-models, and MongoApp to Java

EOF
)"
```

---

### Task 6: module-info, remove Kotlin/H2 artifacts, verify

**Files:**
- Modify: `src/main/java/module-info.java` (move/replace existing `src/main/java/module-info.java`)
- Delete: `src/main/kotlin/**`
- Delete: `src/main/resources/db/config.sql`
- Modify: FXML only if a controller FQCN must change (should not)

**Interfaces:**
- Consumes: full app
- Produces: clean Java-only tree that `mvn test package` succeeds

- [ ] **Step 1: Update `module-info.java`**

Open module (or explicit opens for FXML packages) named consistently with javafx plugin (`brane.fx.mongo` or align to package — **keep module name `brane.fx.mongo`** unless pom mainClass module prefix forces otherwise; fix pom `mainClass` to `brane08.fx.mongo.MongoApp` without wrong module prefix if using non-modular run). `requires` for java.sql only if still needed (drop if unused), javafx.*, guice, typesafe config, mongodb, slf4j, ikonli; remove kotlin/exposed. `opens` packages to `javafx.fxml` and guice as needed.

- [ ] **Step 2: Delete Kotlin sources and H2 SQL**

Remove `src/main/kotlin` tree and `src/main/resources/db/config.sql`.

- [ ] **Step 3: Full verify**

Run: `mvn -q test package`  
Expected: PASS; artifact built; no kotlin/exposed/h2 in `mvn dependency:tree` output

Run: `mvn -q dependency:tree`  
Expected: no `kotlin-stdlib`, `exposed-*`, or `h2` artifacts

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
chore: drop Kotlin/H2 and finish Java module setup

EOF
)"
```

---

## Self-review notes (author)

- Spec coverage: Java port, HOCON+ENC, home dir, upsert, latest libs, Guice/FXML preserved, no execute impl, no H2 migration — all tasked.
- Review Focus items mapped to ConfigServiceTest / SecretBoxTest assertions.
- `MongoService.testConnection(Connection)` username bugfix called out explicitly in Task 3.
