package com.mallowigi.clickup.toolwindow

import com.mallowigi.clickup.api.model.SpaceTree

/** Immutable state driving the ClickUp tool window UI. */
sealed interface ClickUpUiState {
  /** No API token configured yet — prompt the user to open Settings. */
  data object NotConfigured : ClickUpUiState

  /** A fetch is in progress. */
  data object Loading : ClickUpUiState

  /** Fetch succeeded but there are no spaces to show. */
  data object Empty : ClickUpUiState

  /** Fetch succeeded with content. */
  data class Content(val tree: SpaceTree) : ClickUpUiState

  /** Fetch failed; [message] is user-facing. */
  data class Error(val message: String) : ClickUpUiState
}
