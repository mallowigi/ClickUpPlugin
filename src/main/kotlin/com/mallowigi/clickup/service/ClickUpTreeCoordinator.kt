package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.model.ClickUpList
import com.mallowigi.clickup.api.model.FolderNode
import com.mallowigi.clickup.api.model.SpaceNode
import com.mallowigi.clickup.api.model.SpaceTree
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Assembles the [SpaceTree] by orchestrating the per-domain services: for every team, fetch
 * its spaces, and for every space fetch its folders (each carrying its lists) plus the
 * folderless lists. Fetches run concurrently but are capped by [requestGate] so we never
 * burst past ClickUp's rate limit.
 *
 * Pure orchestration over the services — holds no transport/endpoint knowledge itself.
 */
@Service(Service.Level.APP)
class ClickUpTreeCoordinator {

  private val teams: TeamsService get() = TeamsService.instance
  private val spaces: SpacesService get() = SpacesService.instance
  private val folders: FoldersService get() = FoldersService.instance
  private val lists: ListsService get() = ListsService.instance

  /** Caps the number of ClickUp requests in flight at once to stay within rate limits. */
  private val requestGate = Semaphore(MAX_CONCURRENT_REQUESTS)

  /** Fetches the full Space → [Folder →] List tree across all of the user's teams. */
  suspend fun loadTree(): SpaceTree = coroutineScope {
    val spaceNodes = teams.getTeams()
      .flatMap { team -> spaces.getSpaces(team.id) }
      .map { space -> async { loadSpace(space.id).let { (f, l) -> SpaceNode(space, f, l) } } }
      .awaitAll()
    SpaceTree(spaceNodes)
  }

  private suspend fun loadSpace(spaceId: String): Pair<List<FolderNode>, List<ClickUpList>> =
    coroutineScope {
      val foldersDeferred = async {
        requestGate.withPermit { folders.getFolders(spaceId) }.map { FolderNode(it, it.lists) }
      }
      val folderlessDeferred = async {
        requestGate.withPermit { lists.getFolderlessLists(spaceId) }
      }
      foldersDeferred.await() to folderlessDeferred.await()
    }

  companion object {
    private const val MAX_CONCURRENT_REQUESTS = 8

    val instance: ClickUpTreeCoordinator
      get() = service()
  }
}
