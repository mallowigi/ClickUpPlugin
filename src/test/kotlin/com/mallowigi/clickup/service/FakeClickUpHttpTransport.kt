package com.mallowigi.clickup.service

import com.mallowigi.clickup.api.ClickUpHttpTransport
import com.mallowigi.clickup.api.ClickUpJson
import kotlinx.serialization.DeserializationStrategy

/**
 * In-memory [ClickUpHttpTransport] for offline tests. Records the requested paths and token,
 * and decodes canned JSON bodies with the production [ClickUpJson] config — so tests exercise
 * real deserialization without touching the network.
 */
class FakeClickUpHttpTransport : ClickUpHttpTransport {

  val requestedPaths: MutableList<String> = mutableListOf()
  var lastToken: String? = null

  private val bodies: MutableMap<String, String> = mutableMapOf()

  /** Stubs the JSON body returned for an exact [path]. */
  fun stub(path: String, json: String) {
    bodies[path] = json
  }

  override fun <T> get(path: String, token: String?, deserializer: DeserializationStrategy<T>): T {
    requestedPaths += path
    lastToken = token
    val body = bodies[path] ?: error("No stubbed response for path: $path")
    return ClickUpJson.decodeFromString(deserializer, body)
  }
}
