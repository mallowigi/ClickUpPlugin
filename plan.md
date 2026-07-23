# ClickUp Plugin — Spaces & Lists Tool Window

## Problem

The project is the standard IntelliJ Platform plugin template (Kotlin, IntelliJ 2025.3.5)
with a placeholder "shuffle number" tool window. We want a first, simple feature: a tool window that talks to the
ClickUp API and displays the user's **Spaces** and the **Lists**
inside each space.

## Approach

Replace the placeholder tool window with a real ClickUp integration:

1. **Auth / settings** — Personal API token (`pk_...`), sent as the raw `Authorization`
   header (no `Bearer` prefix). Store it via IntelliJ's `PasswordSafe` (secure) and expose a small Settings
   (Configurable) page **built with Kotlin UI DSL v2** (pure Swing, no Compose) to enter it. Never log or commit the
   token.

2. **API client** — A thin HTTP client (using the JDK `java.net.http.HttpClient`) plus Kotlin data models. Endpoints
   (ClickUp API v2, base `https://api.clickup.com/api/v2`):
    - `GET /team` → workspaces/teams (needed to reach spaces)
    - `GET /team/{team_id}/space` → spaces
    - `GET /space/{space_id}/folder` → folders
    - `GET /folder/{folder_id}/list` → lists inside a folder
    - `GET /space/{space_id}/list` → folderless lists JSON parsing via kotlinx.serialization (added as a dependency).

3. **Domain model** — Fetch teams → spaces → (folderless lists + folders → lists) and assemble a tree:
   `Space > [Folder >] List`.

4. **Tool window UI (Jewel / Compose)** — Render Spaces with their Lists using **Jewel**
   via the IDE laf-bridge (Compose for Desktop) inside the tool window. Fetch on a background coroutine/thread and drive
   Compose state; show loading / empty / error states and a Refresh action, plus a "Configure token" prompt when no
   token is set. Dispose the Compose panel with the tool window content.

5. **Plugin metadata** — Rename the tool window from "MyToolWindow" to a ClickUp window, update `plugin.xml`, message
   bundle, and remove the placeholder shuffle code.

## UI toolkit split (confirmed)

Two independent UI surfaces, each using the best-fit toolkit — no coupling beyond shared data (token + API client):

- **Settings page → Kotlin UI DSL v2** (pure Swing `JComponent` from the `Configurable`; no Compose, so no Jewel
  cold-start/memory cost on a simple form).
- **Tool window → Jewel / Compose** (Compose panel hosted in the tool window via the Jewel IDE laf-bridge). The tool
  window owns the Compose lifecycle and disposes the panel with its content; the settings page stays pure Swing.

## UI framework: decision = Jewel (Compose for Desktop) for the tool window

**Decision: build the tool window with Jewel**, as a deliberate learning use case for a greenfield project. Use the
**IDE laf-bridge** integration so the UI mirrors the current IDE theme.

### Performance assessment (why it's acceptable here)

- **One-time cold start:** Compose + Skiko (Skia) initialize the first time the tool window opens — a one-off penalty,
  not per-render/per-refresh, and off the IDE's critical startup path since the window opens on demand.
- **Memory:** Compose/Skia have a somewhat higher fixed memory floor than pure Swing; real but modest, and largely
  independent of how simple the UI is.
- **Runtime rendering:** Reported Jewel/Compose lag is tied to heavy animations / complex layouts (and general 2025.x
  IDE issues), not static content. A Spaces→Lists tree is trivial to render and won't hit those cases.
- **Do NOT bundle Compose:** depend on `jewel-ide-laf-bridge` and use the platform-provided Compose runtime (from the
  JetBrains Runtime). This keeps the plugin small, avoids duplicate-classloader conflicts, and auto-mirrors the IDE
  theme.
- **Verdict:** one-time init + a bit of RAM, not runtime lag → acceptable for this feature.

### Implementation watch-outs

- Add the Kotlin **Compose compiler** plugin; declare Jewel/Compose deps as provided-by-platform (compileOnly /
  `intellijPlatform`-provided), not bundled.
- Verify the exact Jewel bridge artifact/version compatible with IntelliJ 2025.3.5 (Jewel now lives in the IntelliJ
  Platform repo).
- Dispose the Compose panel with the tool window content; keep API/data work off the Compose UI thread.
- Keep the API client + domain model UI-agnostic so the UI stays a thin Compose layer.

## Progress — where we left off (session resume)

**Done & committed** (HEAD `9656a03`):

- Steps 1–5 complete. Build config (Jewel/Compose bundled modules, kotlinx.serialization), API data models
  (Team/Space/Folder/ClickUpList/User/Responses), HTTP transport + client + JSON,
  `ClickUpTokenStorage` (PasswordSafe), per-domain services (Teams/Spaces/Folders/Lists),
  `ClickUpAuthService`, `ClickUpSettingsConfigurable` (Kotlin UI DSL v2, async token validation), and unit tests
  (`ClickUpJsonTest`, `ClickUpServicesTest`, `FakeClickUpHttpTransport`).

**Done but NOT yet committed** (untracked working tree):

- `api/model/SpaceTree.kt` — presentation tree (SpaceTree/SpaceNode/FolderNode).
- `service/ClickUpTreeCoordinator.kt` — assembles the Space→[Folder→]List tree (concurrent per-space).
- `toolwindow/ClickUpUiState.kt` — sealed UI state (NotConfigured/Loading/Empty/Content/Error).
- `toolwindow/ClickUpToolWindowModel.kt` — toolkit-agnostic StateFlow model with refresh + Job cancel.
- `toolwindow/ClickUpToolWindowContent.kt` — Jewel/Compose renderer (Refresh + Configure + tree).

**Remaining to finish Step 6–8:**
- A) **ToolWindowFactory** hosting the Jewel/Compose panel: create `JewelComposePanel`, wire
  `ClickUpToolWindowModel` → `ClickUpToolWindowContent`, provide `onRefresh`/`onConfigure`
  (open Settings), trigger initial `refresh()`, dispose panel with tool window content, use a platform-managed lifecycle
  `CoroutineScope` bound to the content `Disposable`, and expose Refresh as a title action (review #3/#4). **This is the
  immediate next task.**
- B) **Register `<toolWindow>`** in `plugin.xml` (id, anchor, factoryClass, icon).
- C) **Message bundle keys** — DONE (toolwindow.* keys added).
- D) **Remove placeholder** shuffle/MyToolWindow code — appears already gone; verify none remains.
- E) **Tests** for `ClickUpTreeCoordinator` tree assembly against fakes.
- F) **Build + `runIde`** smoke test (offline/error state expected; happy path needs unfirewalled machine).

**Self-review fixes applied:**
- #1 `ClickUpToolWindowModel.refresh()` now catches all exceptions (rethrowing `CancellationException`)
  so unexpected failures surface as `Error` instead of hanging on `Loading`.
- #2 `ClickUpTreeCoordinator` fan-out now bounded by a `Semaphore(8)` request gate to respect rate limits.
- #5 `ClickUpUiState.Error.message` annotated `@Nls`.
- #6 `SpaceRow` indentation via `Modifier.padding(start=…)` instead of literal spaces.
- #8 coordinator imports cleaned / `awaitAll` (already present).
- Deferred: #3/#4 (factory scope + title action), #7 layering, #9 dead-code review → tracked in SQL.

**Debugging fixes (build):**

- Light services must be `final`. Reverted the constructor-injection experiment: `TeamsService`,
  `SpacesService`, `FoldersService`, `ListsService`, `ClickUpApiService`, and `ClickUpAuthService`
  are plain final light services again (base resolves transport/token directly). Per user, the test seam was dropped:
  `ClickUpServicesTest` + `FakeClickUpHttpTransport` removed. `ClickUpJsonTest`
  remains as the offline validation.
- `instrumentCode = false` in `build.gradle.kts` — the platform bytecode instrumenter can't read Java 25 class files
  (fails with cryptic `1 >= 1`); we don't use form/@NotNull instrumentation.
- `./gradlew clean test` now BUILD SUCCESSFUL.

## Todos

Tracked in SQL (`todos` table). High level:

1. Add dependencies: kotlinx.serialization + Jewel (IDE laf-bridge) & Compose compiler plugin.
2. Define ClickUp API data models (Team, Space, Folder, ClickUpList).
3. Implement token storage + Settings Configurable.
4. Implement the ClickUp HTTP API client.
5. Implement a repository/service that assembles Space → Lists tree.
6. Build the tool window UI with Jewel/Compose (loading/error/empty + Refresh).
7. Update plugin.xml, message bundle; remove placeholder code.
8. Build & smoke-test in a sandbox IDE (runIde).

## Testing strategy (ClickUp API is firewalled on this machine — confirmed)

Live calls to `api.clickup.com` are blocked here (TLS handshake refused). Build repos (Maven Central, JetBrains) ARE
reachable, so building/dependency resolution is fine.

- **Unit tests:** validate the API client, JSON parsing, and tree-assembly against canned JSON — no network. Primary
  validation.
- **Offline UI validation:** put the API behind a `ClickUpApi` interface with a real HTTP impl and a `FakeClickUpApi`
  returning sample spaces/lists. A dev/offline toggle lets
  `runIde` render the Jewel tool window with canned data (no network).
- **Error-state check:** a real fetch here lands in the error state — useful to verify error handling, but
  happy-path-against-real-account must be checked on an unfirewalled machine.

## Notes / considerations

- Keep the API client and models UI-agnostic; the Jewel UI stays a thin presentation layer.
- Handle token-missing and HTTP error (401/429) states gracefully.
- Respect ClickUp rate limits; do minimal calls (fetch lists lazily per space if needed).
- No token or secret in logs, code, or VCS.
- Decide default: show all teams' spaces, or the first team's. Start with all teams.

## BLOCKER (RESOLVED): JVM TLS trust vs corporate proxy

- The machine is behind a Palo Alto **Prisma** TLS-intercepting proxy (certs issued by
  `CN=mci-prisma / MasterCard Worldwide`). JVM/Gradle fails with `PKIX path building failed`
  because Java's `cacerts` lacks that root; Gradle masks it as "plugin not found".
- **Resolution used:** disconnect the VPN during dependency downloads (user action). When the VPN is on, JVM downloads
  fail — if it reappears, STOP and notify the user.
- Working build config (Step 1 done): apply `kotlin.plugin.compose` + `org.jetbrains.compose`
  (1.7.1) + `kotlin.plugin.serialization`; repos add `google()` and JetBrains KPM; Jewel via
  `bundledModule(...)` (foundation, ui, ideLafBridge, compose.foundation.desktop, skiko).
