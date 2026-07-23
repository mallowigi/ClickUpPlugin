package com.mallowigi.clickup.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A List, either inside a [Folder] or directly under a [Space] (folderless). */
@Serializable
data class ClickUpList(
  val id: String,
  val name: String,
  @SerialName("task_count") val taskCount: Int? = null,
)
