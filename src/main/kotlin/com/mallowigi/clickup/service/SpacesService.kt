package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.Space
import com.mallowigi.clickup.api.model.SpacesResponse

/** Talks to the ClickUp space endpoints. */
@Service(Service.Level.APP)
class SpacesService : ClickUpApiService() {

  /** GET /team/{teamId}/space — the spaces within a team. */
  suspend fun getSpaces(teamId: String): List<Space> =
    get("/team/$teamId/space", SpacesResponse.serializer()).spaces

  companion object {
    val instance: SpacesService
      get() = service()
  }
}
