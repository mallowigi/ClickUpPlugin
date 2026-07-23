package com.mallowigi.clickup.service

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service

/**
 * Application-level service that securely stores the ClickUp personal API token via the
 * platform [PasswordSafe] (system keychain / encrypted store). The token is never
 * persisted in plain settings or logged.
 */
@Service(Service.Level.APP)
class ClickUpTokenStorage {

  private val credentialAttributes: CredentialAttributes =
    CredentialAttributes(generateServiceName(SUBSYSTEM, KEY))

  /** The stored token, or `null`/blank if none is configured. */
  fun getToken(): String? = PasswordSafe.instance.getPassword(credentialAttributes)

  /** Stores [token], or clears it when [token] is null or blank. */
  fun setToken(token: String?) {
    PasswordSafe.instance.setPassword(credentialAttributes, token?.takeIf { it.isNotBlank() })
  }

  fun hasToken(): Boolean = !getToken().isNullOrBlank()

  companion object {
    private const val SUBSYSTEM = "ClickUp"
    private const val KEY = "apiToken"
  }
}
