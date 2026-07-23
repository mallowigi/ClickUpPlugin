package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.Team
import com.mallowigi.clickup.api.model.TeamsResponse

/** Talks to the ClickUp `/team` endpoints. */
@Service(Service.Level.APP)
open class TeamsService : ClickUpApiService() {

  /** GET /team — the workspaces (teams) the authenticated user belongs to. */
  suspend fun getTeams(): List<Team> = get("/team", TeamsResponse.serializer()).teams

  companion object {
    val instance: TeamsService
      get() = service()
  }
}
