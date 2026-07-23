package com.mallowigi.clickup.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A Space within a [Team]. */
@Serializable
data class Space(
  val id: String,
  val name: String,
  @SerialName("private") val isPrivate: Boolean = false,
)
