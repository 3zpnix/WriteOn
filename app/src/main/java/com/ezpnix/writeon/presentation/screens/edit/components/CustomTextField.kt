package com.ezpnix.writeon.presentation.screens.edit.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    singleLine: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hideContent: Boolean = false
) {
    val visualTransformation = if (hideContent) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    TextField(
        value = value,
        visualTransformation = visualTransformation,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        placeholder = {
            Text(placeholder)
        }
    )
}

class UndoRedoState {
    var input by mutableStateOf(TextFieldValue(""))
    private val undoHistory = ArrayDeque<TextFieldValue>()
    private val redoHistory = ArrayDeque<TextFieldValue>()

    init {
        undoHistory.add(input)
    }

    fun onInput(value: TextFieldValue) {
        val updatedValue = value.copy(value.text, selection = TextRange(value.text.length))
        undoHistory.add(updatedValue)
        redoHistory.clear()
        input = updatedValue
    }

    fun undo() {
        if (undoHistory.size > 1) {
            val lastState = undoHistory.removeLastOrNull()
            lastState?.let {
                redoHistory.add(it)
            }

            val previousState = undoHistory.lastOrNull()
            previousState?.let {
                input = it
            }
        }
    }

    fun redo() {
        val redoState = redoHistory.removeLastOrNull()
        redoState?.let {
            undoHistory.add(it)
            input = it
        }
    }
}
