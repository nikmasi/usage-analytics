package org.example.usageanalytics.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "UsageAnalyticsSettings",
    storages = [Storage("usage-analytics-settings.xml")]
)
class UsageAnalyticsSettings : PersistentStateComponent<UsageAnalyticsSettings.State> {
    companion object {
        fun getInstance(): UsageAnalyticsSettings =
            ApplicationManager.getApplication().
            getService(UsageAnalyticsSettings::class.java)
    }

    data class State(
        var enabled: Boolean = true,
        var topN: Int = 10,
        var ignoredPrefixes: MutableList<String> = mutableListOf(
            "MainToolbar", "MainMenu", "ToolWindow", "Popup"
        )
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) { this.state = state }
}