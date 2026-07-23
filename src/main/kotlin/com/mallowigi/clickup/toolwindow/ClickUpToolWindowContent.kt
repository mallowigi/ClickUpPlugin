package com.mallowigi.clickup.toolwindow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mallowigi.clickup.ClickUpBundle
import com.mallowigi.clickup.api.model.SpaceNode
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text

/**
 * Root Composable for the ClickUp tool window. Renders the current [ClickUpUiState] and offers
 * a Refresh action; a "Configure token" prompt is shown when no token is set.
 */
@Composable
fun ClickUpToolWindowContent(
  stateFlow: StateFlow<ClickUpUiState>,
  onRefresh: () -> Unit,
  onConfigure: () -> Unit,
) {
  val state by stateFlow.collectAsState()

  Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      DefaultButton(onClick = onRefresh) { Text(ClickUpBundle.message("toolwindow.refresh")) }
    }

    when (val s = state) {
      is ClickUpUiState.NotConfigured -> NotConfigured(onConfigure)
      is ClickUpUiState.Loading       -> Text(ClickUpBundle.message("toolwindow.loading"))
      is ClickUpUiState.Empty         -> Text(ClickUpBundle.message("toolwindow.empty"))
      is ClickUpUiState.Error         -> Text(ClickUpBundle.message("toolwindow.error", s.message))
      is ClickUpUiState.Content       -> SpacesTree(s)
    }
  }
}

@Composable
private fun NotConfigured(onConfigure: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(ClickUpBundle.message("toolwindow.notConfigured"))
    DefaultButton(onClick = onConfigure) { Text(ClickUpBundle.message("toolwindow.configure")) }
  }
}

@Composable
private fun SpacesTree(content: ClickUpUiState.Content) {
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    content.tree.spaces.forEach { SpaceRow(it) }
  }
}

@Composable
private fun SpaceRow(node: SpaceNode) {
  Column(Modifier.padding(vertical = 2.dp)) {
    Text(node.space.name, color = JewelTheme.globalColors.text.normal)
    node.folders.forEach { folder ->
      Text(
        folder.folder.name,
        color = JewelTheme.globalColors.text.info,
        modifier = Modifier.padding(start = 12.dp),
      )
      folder.lists.forEach { list ->
        Text(list.name, modifier = Modifier.padding(start = 24.dp))
      }
    }
    node.folderlessLists.forEach { list ->
      Text(list.name, modifier = Modifier.padding(start = 12.dp))
    }
  }
}
