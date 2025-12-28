package org.example.usageanalytics.services

import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.example.usageanalytics.listeners.ToolListener
import org.example.usageanalytics.listeners.UsageListener

@Service(Service.Level.APP)
@State(
    name = "UsageAnalyticsState", storages = [Storage("usage-analytics.xml")]
)
class UsageService : PersistentStateComponent<UsageService.State> {

    init {
        val connection = ApplicationManager.getApplication()
            .messageBus.connect()

        connection.subscribe(
            AnActionListener.TOPIC,
            UsageListener()
        )
    }

    private var state = State()

    data class State(var counters: MutableMap<String, Int> = mutableMapOf())

    override fun getState(): State = state

    override fun loadState(state: State) { this.state = state }

    fun getStats(): Map<String, Int> { return state.counters.toMap() }

    fun increment(actionId: String) {
        val current = state.counters[actionId] ?: 0
        state.counters[actionId] = current + 1

        // event emit
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(ToolListener.Companion.TOPIC)
            .statsUpdated()
    }

    fun clear() {
        state.counters.clear()

        // event emit
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(ToolListener.TOPIC)
            .statsUpdated()
    }
}