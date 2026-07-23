package com.mallowigi.clickup.api

/**
 * Thrown when a ClickUp API call fails (transport error, non-2xx response, or a
 * missing token). Carries an optional HTTP [statusCode] when one is available.
 */
class ClickUpApiException(
  message: String,
  val statusCode: Int? = null,
  cause: Throwable? = null,
) : Exception(message, cause)
