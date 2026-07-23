package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.ClickUpList
import com.mallowigi.clickup.api.model.ListsResponse

/** Talks to the ClickUp list endpoints (folder-based and folderless). */
@Service(Service.Level.APP)
open class ListsService : ClickUpApiService() {

  /** GET /space/{spaceId}/list — the folderless lists directly under a space. */
  suspend fun getFolderlessLists(spaceId: String): List<ClickUpList> =
    get("/space/$spaceId/list", ListsResponse.serializer()).lists

  /** GET /folder/{folderId}/list — the lists within a folder. */
  suspend fun getLists(folderId: String): List<ClickUpList> =
    get("/folder/$folderId/list", ListsResponse.serializer()).lists

  companion object {
    val instance: ListsService
      get() = service()
  }
}
