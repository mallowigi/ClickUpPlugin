package com.mallowigi.clickup.api

import kotlinx.serialization.DeserializationStrategy

/**
 * Endpoint-agnostic transport over the ClickUp API v2. It knows how to perform authenticated
 * HTTP calls and decode JSON responses, but nothing about specific ClickUp resources (teams,
 * spaces, ...). Domain knowledge lives in the per-domain services.
 *
 * Exists primarily as a test seam: production uses the [ClickUpHttpClient] singleton, while
 * tests can substitute a fake.
 *
 * All calls are blocking and must be invoked off the EDT / Compose UI thread.
 */
interface ClickUpHttpTransport {
  /**
   * Performs a GET against [path] (relative to the API base URL), authenticating with [token],
   * and decodes the response body using [deserializer].
   */
  fun <T> get(path: String, token: String?, deserializer: DeserializationStrategy<T>): T
}
