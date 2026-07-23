package com.mallowigi.clickup.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.mallowigi.clickup.ClickUpBundle
import com.mallowigi.clickup.api.ClickUpApiException
import com.mallowigi.clickup.api.model.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Validates ClickUp API tokens by calling `GET /user`, the cheapest authenticated endpoint.
 *
 * The platform injects a [CoroutineScope] tied to this service's lifecycle so the async
 * "Test Connection" flow is cancelled automatically when the plugin unloads.
 */
@Service(Service.Level.APP)
class ClickUpAuthService(private val scope: CoroutineScope) : ClickUpApiService() {

  /** Validates [token] against ClickUp, mapping the outcome to a [TestConnectionResult]. */
  suspend fun testConnection(token: String): TestConnectionResult =
    try {
      val user = get("/user", token, UserResponse.serializer()).user
      TestConnectionResult.Success(user.displayName)
    } catch (e: ClickUpApiException) {
      TestConnectionResult.Failure(userMessageFor(e))
    }

  /**
   * Convenience for Swing callers (e.g. the Settings page): runs [testConnection] on the
   * service scope and delivers [onResult] back on the EDT.
   */
  fun testConnectionAsync(token: String, onResult: (TestConnectionResult) -> Unit) {
    scope.launch {
      val result = testConnection(token)
      ApplicationManager.getApplication().invokeLater({ onResult(result) }, ModalityState.any())
    }
  }

  private fun userMessageFor(e: ClickUpApiException): String = when (e.statusCode) {
    401  -> ClickUpBundle.message("settings.test.invalidToken")
    else -> e.message ?: ClickUpBundle.message("settings.test.unknownError")
  }

  companion object {
    val instance: ClickUpAuthService
      get() = service()
  }
}
