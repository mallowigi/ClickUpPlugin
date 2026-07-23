package com.mallowigi.clickup.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.mallowigi.clickup.ClickUpBundle
import com.mallowigi.clickup.service.ClickUpAuthService
import com.mallowigi.clickup.service.ClickUpTokenStorage
import com.mallowigi.clickup.service.TestConnectionResult
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent

/**
 * Application settings page for entering the ClickUp API token. Built with Kotlin UI DSL v2
 * (pure Swing). The token is persisted via [ClickUpTokenStorage] (PasswordSafe); a
 * "Test Connection" button validates it against ClickUp without saving.
 */
class ClickUpSettingsConfigurable : Configurable {

  private val tokenField = JBPasswordField()
  private val statusLabel = JBLabel()
  private var testButton: JButton? = null

  /** Cached copy of the persisted token, loaded in [reset]; keeps [isModified] off the keychain. */
  private var savedToken: String = ""

  override fun getDisplayName(): String = ClickUpBundle.message("settings.displayName")

  override fun createComponent(): JComponent = panel {
    row(ClickUpBundle.message("settings.token.label")) {
      cell(tokenField)
        .columns(COLUMNS_LARGE)
        .comment(ClickUpBundle.message("settings.token.comment"))
        .focused()
    }
    row {
      testButton = button(ClickUpBundle.message("settings.testConnection")) {
        onTestConnection()
      }.component
      cell(statusLabel)
    }
  }

  private fun onTestConnection() {
    val token = String(tokenField.password).trim()
    if (token.isEmpty()) {
      showStatus(AllIcons.General.Warning, ClickUpBundle.message("settings.test.empty"))
      return
    }
    testButton?.isEnabled = false
    showStatus(null, ClickUpBundle.message("settings.test.testing"))
    service<ClickUpAuthService>().testConnectionAsync(token) { result ->
      testButton?.isEnabled = true
      when (result) {
        is TestConnectionResult.Success ->
          showStatus(AllIcons.General.InspectionsOK, ClickUpBundle.message("settings.test.success", result.userName))

        is TestConnectionResult.Failure ->
          showStatus(AllIcons.General.Error, ClickUpBundle.message("settings.test.failure", result.message))
      }
    }
  }

  private fun showStatus(icon: Icon?, text: String) {
    statusLabel.icon = icon
    statusLabel.text = text
  }

  override fun isModified(): Boolean = String(tokenField.password) != savedToken

  override fun apply() {
    val token = String(tokenField.password)
    service<ClickUpTokenStorage>().setToken(token)
    savedToken = token
  }

  override fun reset() {
    savedToken = service<ClickUpTokenStorage>().getToken().orEmpty()
    tokenField.text = savedToken
    showStatus(null, "")
  }
}
