package com.dnfapps.arrmatey.ui.components.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject

@Composable
fun DoubleBackToExit(moko: MokoStrings = koinInject()) {
    val context = LocalContext.current
    // Track the time of the last back press
    val lastBackPressTime = remember { mutableLongStateOf(0L) }

    PredictiveBackHandler { progress ->
        try {
            progress.collect { }
            val currentTime = System.currentTimeMillis()
            // Check if the difference is less than 2 seconds (2000ms)
            if (currentTime - lastBackPressTime.longValue < 2000) {
                // Exit the app
                (context as? Activity)?.finish()
            } else {
                // Update the timestamp and show the toast
                lastBackPressTime.longValue = currentTime
                Toast
                    .makeText(
                        context,
                        moko.getString(MR.strings.press_again_to_exit),
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        } catch (_: kotlinx.coroutines.CancellationException) {
            // Gesture cancelled
        }
    }
}
