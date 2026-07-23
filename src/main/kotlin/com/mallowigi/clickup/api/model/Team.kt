package com.mallowigi.clickup.api.model

import kotlinx.serialization.Serializable

/** A ClickUp workspace (called a "team" in the API v2). */
@Serializable
data class Team(
  val id: String,
  val name: String,
  val color: String? = null,
  val avatar: String? = null,
)
