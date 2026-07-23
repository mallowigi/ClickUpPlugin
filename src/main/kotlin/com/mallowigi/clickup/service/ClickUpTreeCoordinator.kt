package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.FolderNode
import com.mallowigi.clickup.api.model.SpaceNode
import com.mallowigi.clickup.api.model.SpaceTree
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Assembles the [SpaceTree] by orchestrating the per-domain services: for every team, fetch
 * its spaces, and for every space fetch its folders (each carrying its lists) plus the
 * folderless lists. Fetches within a space run concurrently.
 *
 * Pure orchestration over the services — holds no transport/endpoint knowledge itself.
 */
@Service(Service.Level.APP)
class ClickUpTreeCoordinator {

  private val teams: TeamsService get() = TeamsService.instance
  private val spaces: SpacesService get() = SpacesService.instance
  private val folders: FoldersService get() = FoldersService.instance
  private val lists: ListsService get() = ListsService.instance

  /** Fetches the full Space → [Folder →] List tree across all of the user's teams. */
  suspend fun loadTree(): SpaceTree = coroutineScope {
    val spaceNodes = teams.getTeams()
      .flatMap { team -> spaces.getSpaces(team.id) }
      .map { space -> async { loadSpace(space.id).let { (f, l) -> SpaceNode(space, f, l) } } }
      .awaitAll()
    SpaceTree(spaceNodes)
  }

  private suspend fun loadSpace(spaceId: String): Pair<List<FolderNode>, List<com.mallowigi.clickup.api.model.ClickUpList>> =
    coroutineScope {
      val foldersDeferred = async {
        folders.getFolders(spaceId).map { FolderNode(it, it.lists) }
      }
      val folderlessDeferred = async { lists.getFolderlessLists(spaceId) }
      foldersDeferred.await() to folderlessDeferred.await()
    }

  companion object {
    val instance: ClickUpTreeCoordinator
      get() = service()
  }
}
