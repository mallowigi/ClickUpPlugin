package com.mallowigi.clickup.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.ClickUpApi
import com.mallowigi.clickup.api.HttpClickUpApi
import com.mallowigi.clickup.api.model.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Application-level entry point for talking to ClickUp.
 *
 * The platform injects a [CoroutineScope] tied to this service's lifecycle, which is the
 * sanctioned way to launch background work: it is cancelled automatically when the plugin
 * is unloaded. Networking is dispatched to [Dispatchers.IO]; the underlying [ClickUpApi]
 * calls are blocking by contract.
 */
@Service(Service.Level.APP)
class ClickUpService(val scope: CoroutineScope) {

  private val tokenStorage: ClickUpTokenStorage get() = service()

  private val api: ClickUpApi = HttpClickUpApi(tokenProvider = { tokenStorage.getToken() })

  /** Whether a token is configured; cheap, safe to call from the EDT. */
  fun isConfigured(): Boolean = tokenStorage.hasToken()

  /** GET /team — suspending wrapper that runs the blocking call off the EDT. */
  suspend fun getTeams(): List<Team> = withContext(Dispatchers.IO) { api.getTeams() }

  companion object {
    @JvmStatic
    fun getInstance(): ClickUpService = service()
  }
}
