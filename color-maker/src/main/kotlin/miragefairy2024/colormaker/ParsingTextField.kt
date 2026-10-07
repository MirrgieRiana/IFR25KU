package miragefairy2024.colormaker

import java.awt.Color
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

class ParsingTextField<T : Any>(defaultValue: T, private val decoder: (String) -> T?, private val encoder: (T) -> String) : JTextField() {
    val value = ObservableValue(defaultValue)
    var errorColor: Color = Color.decode("#FFBFBF")

    init {

        // Documentが変更される可能性があるため、それに追従する
        val dl = object : DocumentListener {
            override fun removeUpdate(e: DocumentEvent) = onUpdate()
            override fun insertUpdate(e: DocumentEvent) = onUpdate()
            override fun changedUpdate(e: DocumentEvent) = onUpdate()
        }
        if (document != null) document.addDocumentListener(dl)
        addPropertyChangeListener("document") { e ->
            (e.oldValue as Document).removeDocumentListener(dl)
            (e.newValue as Document).addDocumentListener(dl)
        }

        addActionListener {
            onUpdate()
        }

        value.register { _, it, source ->
            if (source == this) return@register
            text = encoder(it)
            setBackground(Color.WHITE)
        }

        value.fire()
    }

    private fun onUpdate() {
        if (value.modifying) return
        val newValue = decoder(text)
        if (newValue != null) value.set(newValue, this)
        setBackground(if (newValue != null) Color.WHITE else errorColor)
    }
}
