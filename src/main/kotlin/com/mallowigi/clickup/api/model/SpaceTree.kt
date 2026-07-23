package com.mallowigi.clickup.api.model

/**
 * Presentation tree assembled from the ClickUp hierarchy: each [SpaceNode] holds its folders
 * (each with their lists) and any folderless lists directly under the space.
 *
 * UI-agnostic on purpose so it can be unit-tested and rendered by any toolkit.
 */
data class SpaceTree(val spaces: List<SpaceNode> = emptyList()) {
  val isEmpty: Boolean get() = spaces.isEmpty()
}

data class SpaceNode(
  val space: Space,
  val folders: List<FolderNode> = emptyList(),
  val folderlessLists: List<ClickUpList> = emptyList(),
)

data class FolderNode(
  val folder: Folder,
  val lists: List<ClickUpList> = emptyList(),
)
