package com.mallowigi.clickup.service

import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.ClickUpHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy

/**
 * Shared base for the per-domain ClickUp services (teams, spaces, folders, lists).
 *
 * Owns the wiring that every domain service needs: resolving the stored token and running
 * the blocking [ClickUpHttpClient] call off the EDT on [Dispatchers.IO]. Subclasses only
 * declare their endpoints; callers own the coroutine context via the suspend API.
 */
abstract class ClickUpApiService {

  /** Suspending GET (using the stored token) that runs the blocking call off the EDT. */
  protected suspend fun <T> get(path: String, deserializer: DeserializationStrategy<T>): T =
    get(path, service<ClickUpTokenStorage>().getToken(), deserializer)

  /** Suspending GET with an explicit [token] — used to validate a not-yet-saved token. */
  protected suspend fun <T> get(path: String, token: String?, deserializer: DeserializationStrategy<T>): T =
    withContext(Dispatchers.IO) { ClickUpHttpClient.get(path, token, deserializer) }
}
