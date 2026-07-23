package com.mallowigi.clickup.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The authenticated ClickUp user, as returned by `GET /user`. */
@Serializable
data class User(
  val id: Long,
  val username: String? = null,
  val email: String? = null,
  val color: String? = null,
  @SerialName("profilePicture") val profilePicture: String? = null,
) {
  /** A human-friendly label, falling back through username → email → a generic default. */
  val displayName: String
    get() = username?.takeIf { it.isNotBlank() }
      ?: email?.takeIf { it.isNotBlank() }
      ?: "ClickUp user"
}
