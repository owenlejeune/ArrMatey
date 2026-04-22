package com.dnfapps.arrmatey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.features.ReleaseNotes
import com.dnfapps.arrmatey.ui.screens.HomeScreen
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme
import dev.icerock.moko.resources.compose.readTextAsState
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App(
    preferences: PreferencesStore = koinInject()
) {
    val showReleaseNotesSheet by preferences.shouldShowReleaseNotes.collectAsStateWithLifecycle(false)

    LaunchedEffect(Unit) {
        preferences.markFirstLaunchComplete()
    }

    ArrMateyTheme {
        HomeScreen()

        if (showReleaseNotesSheet) {
            ModalBottomSheet(
                onDismissRequest = { preferences.markReleaseNotesAsSeen() }
            ) {
                val update = ReleaseNotes.latestUpdate
                val releaseNotes by update.contentFile.readTextAsState()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = update.title,
                        style = MaterialTheme.typography.headlineMediumEmphasized
                    )
                    MarkdownText(
                        markdown = releaseNotes ?: ""
                    )
                }
            }
        }
    }
}