package com.dnfapps.arrmatey.ui.components.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun DoubleBackToExit(
    openDrawerInstead: Boolean = false,
    closeOverlayInstead: Boolean = false,
    moko: MokoStrings = koinInject(),
    navigationManager: NavigationManager = koinInject(),
) {
    val context = LocalContext.current
    val lastBackPressTime = remember { mutableLongStateOf(0L) }

    PredictiveBackHandler { progress ->
        try {
            progress.collect { }
            if (openDrawerInstead) {
                navigationManager.openDrawer()
            } else if (closeOverlayInstead) {
                navigationManager.closeOverlay()
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime.longValue < 2000) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressTime.longValue = currentTime
                    Toast
                        .makeText(
                            context,
                            moko.getString(MR.strings.press_again_to_exit),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        } catch (_: CancellationException) {
            // Gesture cancelled
        }
    }
}
