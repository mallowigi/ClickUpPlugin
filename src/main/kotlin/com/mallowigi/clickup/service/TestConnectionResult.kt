package com.mallowigi.clickup.service

/** Outcome of validating an API token against ClickUp. */
sealed interface TestConnectionResult {
  /** The token is valid; [userName] identifies the authenticated account. */
  data class Success(val userName: String) : TestConnectionResult

  /** The token was rejected or the request failed; [message] is user-facing. */
  data class Failure(val message: String) : TestConnectionResult
}
