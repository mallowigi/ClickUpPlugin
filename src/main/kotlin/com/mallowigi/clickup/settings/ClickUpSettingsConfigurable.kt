package com.mallowigi.clickup.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
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
import kotlinx.coroutines.Job
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent

/**
 * Application settings page for entering the ClickUp API token. Built with Kotlin UI DSL v2
 * (pure Swing). The token is persisted via [ClickUpTokenStorage] (PasswordSafe); a
 * "Test Connection" button validates it against ClickUp without saving.
 *
 * UI components are created lazily in [createComponent] and released in [disposeUIResources].
 * PasswordSafe (keychain) access is kept off the EDT, and the async test result is delivered
 * under the dialog's modality with its [Job] cancelled on disposal.
 */
class ClickUpSettingsConfigurable : Configurable {

  private var tokenField: JBPasswordField? = null
  private var statusLabel: JBLabel? = null
  private var testButton: JButton? = null
  private var connectionJob: Job? = null

  /** Cached copy of the persisted token, loaded off-EDT in [reset]; keeps [isModified] cheap. */
  private var savedToken: String = ""

  override fun getDisplayName(): String = ClickUpBundle.message("settings.displayName")

  override fun createComponent(): JComponent {
    val field = JBPasswordField().also { tokenField = it }
    val label = JBLabel().also { statusLabel = it }

    return panel {
      row(ClickUpBundle.message("settings.token.label")) {
        cell(field)
          .columns(COLUMNS_LARGE)
          .comment(ClickUpBundle.message("settings.token.comment"))
          .focused()
      }
      row {
        testButton = button(ClickUpBundle.message("settings.testConnection")) {
          onTestConnection()
        }.component
        cell(label)
      }
    }
  }

  private fun onTestConnection() {
    val field = tokenField ?: return
    val button = testButton ?: return
    val token = field.readAndClear().trim()
    if (token.isEmpty()) {
      showStatus(AllIcons.General.Warning, ClickUpBundle.message("settings.test.empty"))
      return
    }

    button.isEnabled = false
    showStatus(null, ClickUpBundle.message("settings.test.testing"))

    val modality = ModalityState.stateForComponent(field)
    connectionJob = service<ClickUpAuthService>().testConnectionAsync(token, modality) { result ->
      val activeButton = testButton ?: return@testConnectionAsync
      activeButton.isEnabled = true

      when (result) {
        is TestConnectionResult.Success ->
          showStatus(AllIcons.General.InspectionsOK, ClickUpBundle.message("settings.test.success", result.userName))
        is TestConnectionResult.Failure ->
          showStatus(AllIcons.General.Error, ClickUpBundle.message("settings.test.failure", result.message))
      }
    }
  }

  private fun showStatus(icon: Icon?, text: String) {
    val label = statusLabel ?: return
    label.icon = icon
    label.text = text
  }

  override fun isModified(): Boolean {
    val field = tokenField ?: return false
    return field.readAndClear() != savedToken
  }

  override fun apply() {
    val field = tokenField ?: return
    val token = field.readAndClear()
    savedToken = token
    ApplicationManager.getApplication().executeOnPooledThread {
      service<ClickUpTokenStorage>().setToken(token)
    }
  }

  override fun reset() {
    val field = tokenField ?: return
    showStatus(null, "")

    val modality = ModalityState.stateForComponent(field)
    ApplicationManager.getApplication().executeOnPooledThread {
      val token = service<ClickUpTokenStorage>().getToken().orEmpty()
      ApplicationManager.getApplication().invokeLater({
                                                        savedToken = token
                                                        tokenField?.text = token
                                                      }, modality)
    }
  }

  override fun disposeUIResources() {
    connectionJob?.cancel()
    connectionJob = null
    tokenField = null
    statusLabel = null
    testButton = null
  }

  /** Reads the field's password and wipes the returned array, minimizing plaintext exposure. */
  private fun JBPasswordField.readAndClear(): String {
    val chars = password
    return try {
      String(chars)
    } finally {
      chars.fill('\u0000')
    }
  }
}
