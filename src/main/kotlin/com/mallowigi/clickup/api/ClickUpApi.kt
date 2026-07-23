package com.mallowigi.clickup.api

import com.mallowigi.clickup.api.model.Team

/**
 * Abstraction over the ClickUp API v2. Kept UI-agnostic so implementations (real
 * HTTP, or an offline fake) can be swapped freely.
 *
 * All calls are blocking and must be invoked off the EDT / Compose UI thread.
 */
interface ClickUpApi {
  /** GET /team — the workspaces (teams) the authenticated user belongs to. */
  fun getTeams(): List<Team>
}
