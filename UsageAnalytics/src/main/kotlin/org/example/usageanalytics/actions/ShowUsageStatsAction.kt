package org.example.usageanalytics.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import org.example.usageanalytics.services.UsageService

class ShowUsageStatsAction: AnAction(){
    override fun actionPerformed(p0: AnActionEvent) {
        val service = ApplicationManager.getApplication()
            .getService(UsageService::class.java)

        val stats = service.getStats()

        val text = if (stats.isEmpty()) {
            "No usage data yet."
        } else {
            stats.entries.joinToString("\n") { (key, value) ->
                "$key : $value"
            }
        }

        Messages.showInfoMessage(text, "IDE Usage Statistics")
    }
}