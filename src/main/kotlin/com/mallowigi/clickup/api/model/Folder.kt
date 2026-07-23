package com.mallowigi.clickup.api.model

import kotlinx.serialization.Serializable

/** A Folder within a [Space]; may contain [ClickUpList]s. */
@Serializable
data class Folder(
  val id: String,
  val name: String,
  val lists: List<ClickUpList> = emptyList(),
)
