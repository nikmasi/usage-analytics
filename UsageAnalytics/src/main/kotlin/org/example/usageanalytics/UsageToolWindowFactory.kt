package org.example.usageanalytics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.example.usageanalytics.listeners.ToolListener
import org.example.usageanalytics.services.UsageAnalyticsSettings
import org.example.usageanalytics.services.UsageService
import java.awt.BorderLayout
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JButton
import javax.swing.JPanel

class UsageToolWindowFactory : ToolWindowFactory {

    private lateinit var textArea: JTextArea
    private lateinit var textTitle: JTextArea

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        textArea = JTextArea().apply { isEditable = false }
        textTitle = JTextArea().apply {JTextArea(" Top "+
                UsageAnalyticsSettings.getInstance().state.topN.toString())}
        val clearButton = JButton("Clear statistics")
        val exportButton = JButton("Export to JSON")

        val buttonPanel = JPanel().apply {
            add(exportButton)
            add(clearButton)
        }

        val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(buttonPanel, BorderLayout.SOUTH)
            add(textTitle, BorderLayout.NORTH)
            add(JScrollPane(textArea), BorderLayout.CENTER)
        }

        val content = ContentFactory.getInstance()
            .createContent(panel, null, false)

        toolWindow.contentManager.addContent(content)

        refresh(textArea)

        val connection = project.messageBus.connect()
        connection.subscribe(
            ToolListener.TOPIC,
            object : ToolListener {
                override fun statsUpdated() {
                    //refresh ui thread
                    ApplicationManager.getApplication().invokeLater {
                        refresh(textArea)
                    }
                }
            }
        )

        clearButton.addActionListener {
            val result = Messages.showYesNoDialog(
                "Are you sure you want to clear all usage statistics?",
                "Clear Usage Statistics",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                ApplicationManager.getApplication()
                    .getService(UsageService::class.java)
                    .clear()
            }
        }

        exportButton.addActionListener {
            val descriptor = FileSaverDescriptor(
                "Export Usage Statistics",
                "Save usage statistics as JSON",
                "json"
            )

            val saveFileDialog = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, null)

            val result = saveFileDialog.save( "usage-stats.json") ?: return@addActionListener

            val service = ApplicationManager.getApplication()
                .getService(UsageService::class.java)
            val stats = service.getStats()

            val json = buildString {
                append("{\n")
                stats.entries.forEachIndexed { index, entry ->
                    append("  \"${entry.key}\": ${entry.value}")
                    if (index != stats.size - 1) append(",")
                    append("\n")
                }
                append("}")
            }

            result.file?.writeText(json)

            Messages.showInfoMessage(
                "Usage statistics exported successfully.",
                "Export Complete"
            )
        }
    }

    private fun refresh(textArea: JTextArea) {
        val service = ApplicationManager.getApplication()
            .getService(UsageService::class.java)

        val stats = service.getStats()
        val settings = UsageAnalyticsSettings.getInstance().state


        if (!settings.enabled) {
            val text = if (stats.isEmpty()) {
                "No usage data yet."
            } else {
                var cnt=1
                stats.entries
                    .sortedByDescending { it.value }
                    .joinToString("\n")
                    { "${cnt++}. ${it.key} : ${it.value}"}
            }

            textArea.text = text
            textTitle.text = " All "
            return
        }else{
            val text = formatStatsAsBarsWithRanking(stats, maxBars = 20, topN = settings.topN)
            textArea.text = text
            textTitle.text = " Top "+
                    UsageAnalyticsSettings.getInstance().state.topN.toString()
        }
    }

    private fun formatStatsAsBarsWithRanking(
        stats: Map<String, Int>,
        maxBars: Int = 20,
        topN: Int = 10
    ): String {
        if (stats.isEmpty()) return "No usage data yet."

        val sortedStats = stats.entries.sortedByDescending { it.value }.take(topN)

        val maxCount = sortedStats.maxOf { it.value }

        val block = "\u2588"
        return sortedStats.mapIndexed { index, (action, count) ->
            val rank = index + 1
            val barLength = ((count.toDouble() / maxCount) * maxBars).toInt().coerceAtLeast(1)
            val bar = block.repeat(barLength)
            String.format("%2d. %-15s %s %d", rank, action, bar, count)
        }.joinToString("\n")
    }
}