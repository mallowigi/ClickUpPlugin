package com.mallowigi.clickup.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.mallowigi.clickup.settings.ClickUpSettingsConfigurable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.jewel.bridge.JewelComposePanel

/**
 * Registers the ClickUp tool window: hosts a Jewel/Compose panel that renders the user's
 * Spaces and Lists. Data is fetched off the UI thread by [ClickUpToolWindowModel]; the panel
 * and its coroutine scope are disposed with the tool window content.
 */
class ClickUpToolWindowFactory : ToolWindowFactory, DumbAware {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val disposable = Disposer.newDisposable("ClickUpToolWindow")
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.EDT)
    Disposer.register(disposable, Disposable { scope.cancel() })

    val model = ClickUpToolWindowModel(scope)
    val panel = JewelComposePanel {
      ClickUpToolWindowContent(
        stateFlow = model.state,
        onRefresh = model::refresh,
        onConfigure = {
          ShowSettingsUtil.getInstance()
            .showSettingsDialog(project, ClickUpSettingsConfigurable::class.java)
        },
      )
    }

    model.refresh()

    val content = ContentFactory.getInstance().createContent(panel, null, false)
    content.setDisposer(disposable)
    toolWindow.contentManager.addContent(content)
  }
}
