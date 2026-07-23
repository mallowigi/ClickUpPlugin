package com.mallowigi.clickup.api

import kotlinx.serialization.json.Json

/**
 * The single [Json] configuration used to decode ClickUp API responses.
 *
 * `ignoreUnknownKeys` — ClickUp returns many fields we don't model; without this, decoding
 * would fail on any unmapped property. `coerceInputValues` — falls back to declared defaults
 * when a value is `null`/invalid for a non-null property.
 */
internal val ClickUpJson: Json = Json {
  ignoreUnknownKeys = true
  coerceInputValues = true
}
