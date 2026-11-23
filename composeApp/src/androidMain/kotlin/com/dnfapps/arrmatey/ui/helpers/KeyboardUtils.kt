package com.dnfapps.arrmatey.ui.helpers

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Extension property for checking if a modifier key (Cmd on macOS or Ctrl on Windows/Linux) is pressed.
 */
val KeyEvent.isCmdOrCtrlPressed: Boolean
    get() = isCtrlPressed || isMetaPressed

/**
 * Converts a digit key ([Key.One]..[Key.Nine]) to its 1-based integer index, or null if not a digit 1-9.
 */
val Key.digitIndex: Int?
    get() = when (this) {
        Key.One -> 1
        Key.Two -> 2
        Key.Three -> 3
        Key.Four -> 4
        Key.Five -> 5
        Key.Six -> 6
        Key.Seven -> 7
        Key.Eight -> 8
        Key.Nine -> 9
        else -> null
    }

/**
 * Applies global app keyboard shortcuts handling to a composable modifier.
 *
 * Shortcuts handled:
 * - Cmd/Ctrl + Comma (`,`): Triggers [onOpenSettings]
 * - Escape (`Esc`): Triggers [onEscape]
 * - Cmd/Ctrl + Number (1-9): Triggers [onSelectTabByIndex] with 0-based tab index
 */
fun Modifier.globalKeyboardShortcuts(
    onOpenSettings: () -> Unit,
    onEscape: () -> Boolean,
    onSelectTabByIndex: (Int) -> Unit,
): Modifier = this.onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown) {
        val isCmdOrCtrl = event.isCmdOrCtrlPressed
        when {
            isCmdOrCtrl && event.key == Key.Comma -> {
                onOpenSettings()
                true
            }

            event.key == Key.Escape -> {
                onEscape()
            }

            isCmdOrCtrl -> {
                val digit = event.key.digitIndex
                if (digit != null) {
                    onSelectTabByIndex(digit - 1)
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    } else {
        false
    }
}
