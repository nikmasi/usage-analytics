package org.example.usageanalytics.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.example.usageanalytics.services.UsageService

class ResetUsageStatsAction : AnAction(){
    override fun actionPerformed(e: AnActionEvent){
        val service = ApplicationManager.getApplication()
            .getService(UsageService::class.java)

        service.clear()
    }
}