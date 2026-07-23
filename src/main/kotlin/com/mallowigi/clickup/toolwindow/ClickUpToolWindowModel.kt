package com.mallowigi.clickup.toolwindow

import com.mallowigi.clickup.ClickUpBundle
import com.mallowigi.clickup.service.ClickUpTokenStorage
import com.mallowigi.clickup.service.ClickUpTreeCoordinator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds the [ClickUpUiState] for the tool window and reloads it on demand. Toolkit-agnostic
 * (exposes a [StateFlow]) so the Compose layer stays a thin renderer and the logic is testable.
 *
 * [scope] is the tool window's lifecycle scope; loads are cancelled with it.
 */
class ClickUpToolWindowModel(
  private val scope: CoroutineScope,
  private val tokenStorage: ClickUpTokenStorage = ClickUpTokenStorage.instance,
  private val coordinator: ClickUpTreeCoordinator = ClickUpTreeCoordinator.instance,
) {

  private val _state = MutableStateFlow<ClickUpUiState>(ClickUpUiState.Loading)
  val state: StateFlow<ClickUpUiState> = _state.asStateFlow()

  private var loadJob: Job? = null

  /** Reloads the tree, transitioning through [ClickUpUiState.Loading]. */
  fun refresh() {
    if (!tokenStorage.hasToken()) {
      _state.value = ClickUpUiState.NotConfigured
      return
    }
    loadJob?.cancel()
    _state.value = ClickUpUiState.Loading
    loadJob = scope.launch {
      _state.value = try {
        val tree = coordinator.loadTree()
        if (tree.isEmpty) ClickUpUiState.Empty else ClickUpUiState.Content(tree)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        ClickUpUiState.Error(e.message ?: ClickUpBundle.message("toolwindow.error.generic"))
      }
    }
  }
}
