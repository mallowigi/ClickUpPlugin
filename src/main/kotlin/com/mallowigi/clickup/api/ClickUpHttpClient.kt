package com.mallowigi.clickup.api

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.io.HttpRequests
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * [ClickUpHttpClient] backed by the platform's proxy-aware [HttpRequests], talking to the
 * ClickUp API v2.
 *
 * Using [HttpRequests] (rather than a raw JDK `HttpClient`) means requests honor the IDE's
 * configured HTTP proxy and trusted certificates -- essential behind corporate proxies.
 *
 * Authentication uses a personal token sent as the raw `Authorization` header (no `Bearer`
 * prefix), per the ClickUp API. The token is supplied by the caller on each call, so this
 * object is a stateless singleton with no dependency on platform credential services.
 *
 * All calls are blocking and must be invoked off the EDT / Compose UI thread.
 */
object ClickUpHttpClient : ClickUpHttpTransport {

  override fun <T> get(path: String, token: String?, deserializer: DeserializationStrategy<T>): T {
    val authToken = token?.takeIf { it.isNotBlank() }
      ?: throw ClickUpApiException("No ClickUp API token configured.")

    val body = try {
      HttpRequests.request(DEFAULT_BASE_URL + path)
        .tuner { it.setRequestProperty("Authorization", authToken) }
        .accept("application/json")
        .connectTimeout(CONNECT_TIMEOUT_MS)
        .readTimeout(READ_TIMEOUT_MS)
        .readString()
    } catch (e: HttpRequests.HttpStatusException) {
      throw ClickUpApiException(
        "ClickUp API request to $path failed with HTTP ${e.statusCode}.",
        statusCode = e.statusCode,
        cause = e,
      )
    } catch (e: IOException) {
      throw ClickUpApiException("Failed to reach ClickUp: ${e.message}", cause = e)
    }

    return try {
      JSON.decodeFromString(deserializer, body)
    } catch (e: Exception) {
      thisLogger().warn("Failed to parse ClickUp response from $path", e)
      throw ClickUpApiException("Failed to parse ClickUp response from $path: ${e.message}", cause = e)
    }
  }

  const val DEFAULT_BASE_URL: String = "https://api.clickup.com/api/v2"

  private const val CONNECT_TIMEOUT_MS = 15_000
  private const val READ_TIMEOUT_MS = 30_000

  private val JSON = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }
}
