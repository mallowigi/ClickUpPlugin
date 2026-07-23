package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.Folder
import com.mallowigi.clickup.api.model.FoldersResponse

/** Talks to the ClickUp folder endpoints. */
@Service(Service.Level.APP)
open class FoldersService : ClickUpApiService() {

  /** GET /space/{spaceId}/folder — the folders within a space. */
  suspend fun getFolders(spaceId: String): List<Folder> =
    get("/space/$spaceId/folder", FoldersResponse.serializer()).folders

  companion object {
    val instance: FoldersService
      get() = service()
  }
}
