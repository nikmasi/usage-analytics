package org.example.usageanalytics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import org.example.usageanalytics.listeners.ToolListener
import org.example.usageanalytics.services.UsageAnalyticsSettings
import java.awt.GridLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JTextField

// Called by the IntelliJ Platform when the user opens FIle->Settings
class UsageAnalyticsConfigurable : Configurable {

    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var topNField: JTextField
    private lateinit var panel: JPanel

    override fun getDisplayName(): String = "IDE Usage Analytics"

    override fun createComponent(): JComponent {
        enabledCheckBox = JCheckBox("Enable usage tracking")
        topNField = JTextField()

        panel = JPanel(GridLayout(2, 2)).apply {
            add(JLabel("Enabled:"))
            add(enabledCheckBox)
            add(JLabel("Top N actions:"))
            add(topNField)
        }

        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val settings = UsageAnalyticsSettings.getInstance().state
        return enabledCheckBox.isSelected != settings.enabled ||
                topNField.text != settings.topN.toString()
    }

    override fun apply() {
        val settings = UsageAnalyticsSettings.getInstance().state
        settings.enabled = enabledCheckBox.isSelected
        settings.topN = topNField.text.toIntOrNull() ?: 10

        // notify toolWindow to refresh
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(ToolListener.TOPIC)
            .statsUpdated()
    }

    override fun reset() {
        val settings = UsageAnalyticsSettings.getInstance().state
        enabledCheckBox.isSelected = settings.enabled
        topNField.text = settings.topN.toString()
    }
}
