package com.mallowigi.clickup.service

import com.intellij.openapi.components.service
import com.mallowigi.clickup.api.ClickUpHttpClient
import com.mallowigi.clickup.api.ClickUpHttpTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy

/**
 * Shared base for the per-domain ClickUp services (teams, spaces, folders, lists).
 *
 * Owns the wiring that every domain service needs: resolving the stored token and running
 * the blocking [ClickUpHttpTransport] call off the EDT on [Dispatchers.IO]. Subclasses only
 * declare their endpoints; callers own the coroutine context via the suspend API.
 */
abstract class ClickUpApiService {

  /** The transport; `open` so tests can substitute a fake. */
  protected open val http: ClickUpHttpTransport = ClickUpHttpClient

  /** Supplies the current API token; `open` so tests can bypass the credential store. */
  protected open val tokenProvider: () -> String? = { service<ClickUpTokenStorage>().getToken() }

  /** Suspending GET that runs the blocking transport call off the EDT. */
  protected suspend fun <T> get(path: String, deserializer: DeserializationStrategy<T>): T =
    withContext(Dispatchers.IO) { http.get(path, tokenProvider(), deserializer) }
}
