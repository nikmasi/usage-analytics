package org.example.usageanalytics.listeners

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import org.example.usageanalytics.services.UsageService
import org.example.usageanalytics.services.UsageAnalyticsSettings

class UsageListener : AnActionListener{

    // Track all real executable actions dynamically.
    private fun filterActions(actionId:String,action:AnAction):Boolean{
        if (actionId == null) return false
        println("ACTION ID = $actionId")

        if (action is ActionGroup) return false
        if (actionId.contains("usageanalytics")) return false
        if (UsageAnalyticsSettings.getInstance().state.ignoredPrefixes.any
            { actionId.startsWith(it) }) return false
        return true
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        val actionId = ActionManager.getInstance().getId(action)
            ?: return

        // filter actions
        if(!filterActions(actionId, action)) return

        val service = ApplicationManager.getApplication()
            .getService(UsageService::class.java)

        service.increment(actionId)
    }
}