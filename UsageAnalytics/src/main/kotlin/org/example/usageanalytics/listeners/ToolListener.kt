package org.example.usageanalytics.listeners

import com.intellij.util.messages.Topic

interface ToolListener {
    fun statsUpdated()

    companion object {
        val TOPIC = Topic.create(
            "Usage stats updated",
            ToolListener::class.java
        )
    }
}
