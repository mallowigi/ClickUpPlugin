package com.mallowigi.clickup.api.model

import kotlinx.serialization.Serializable

/**
 * Response wrappers for the ClickUp API v2 endpoints. Each collection is nested
 * under a named property in the JSON payload.
 */

@Serializable
data class UserResponse(val user: User)

@Serializable
data class TeamsResponse(val teams: List<Team> = emptyList())

@Serializable
data class SpacesResponse(val spaces: List<Space> = emptyList())

@Serializable
data class FoldersResponse(val folders: List<Folder> = emptyList())

@Serializable
data class ListsResponse(val lists: List<ClickUpList> = emptyList())
