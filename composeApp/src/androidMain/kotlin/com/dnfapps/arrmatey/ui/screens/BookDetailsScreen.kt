package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.viewmodel.BookDetailsViewModel
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DetailHeaderBanner
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.ReleaseDownloadButtons
import com.dnfapps.arrmatey.ui.helpers.LocalIsInTwoPane
import com.dnfapps.arrmatey.utils.dp
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookDetailsScreen(
    book: Book,
    author: Author,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    onBack: () -> Unit = {},
    onNavigateToBookRelease: (Long) -> Unit = {},
    viewModel: BookDetailsViewModel = koinViewModel(key = "${author.id}_${book.id}", parameters = { parametersOf(author.id, book) }),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentBook by viewModel.book.collectAsStateWithLifecycle()
    val bookFiles by viewModel.bookFiles.collectAsStateWithLifecycle()
    val bookEdition by viewModel.bookEdition.collectAsStateWithLifecycle()

    val history by viewModel.history.collectAsStateWithLifecycle()
    val monitorStatus by viewModel.monitorStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(monitorStatus) {
        when (val status = monitorStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, status.message ?: "Updated", Toast.LENGTH_SHORT).show()
                viewModel.resetMonitorStatus()
            }
            is OperationStatus.Error -> {
                status.message?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                viewModel.resetMonitorStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteStatus) {
        when (val status = deleteStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, status.message ?: "Deleted", Toast.LENGTH_SHORT).show()
            }
            is OperationStatus.Error -> {
                status.message?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            OverlayTopAppBar(
                scrollState = scrollState,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMonitor() },
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = if (currentBook.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = { confirmDelete = true },
                        colors =
                            IconButtonDefaults.headerBarColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        enabled = bookFiles.isNotEmpty(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues.copy(top = 0.dp, bottom = 0.dp))
                    .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isInTwoPane = LocalIsInTwoPane.current
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    DetailHeaderBanner(
                        bannerUrl = currentBook.getCover()?.remoteUrl,
                        gradientHeight = 100.dp,
                        startGradient = isExpanded && (wideRailIsVisible || isInTwoPane),
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column {
                        Text(
                            text = currentBook.title.breakable(),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        currentBook.author?.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        currentBook.pageCount?.let { pageCount ->
                            Text(
                                text = "$pageCount pages",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    bookEdition?.overview?.let { overview ->
                        ItemDescriptionCard(overview)
                    }

                    ReleaseDownloadButtons(
                        onInteractiveClicked = {
                            onNavigateToBookRelease(currentBook.id)
                        },
                        onAutomaticClicked = {
                            viewModel.executeAutomaticSearch()
                        },
                        automaticSearchEnabled = currentBook.monitored,
                    )

                    Text(
                        text = mokoString(MR.strings.files),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (bookFiles.isNotEmpty()) {
                        bookFiles.forEach { file ->
                            BookFileCard(file)
                        }
                    } else {
                        Text(
                            text = mokoString(MR.strings.no_files),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    when (val historyResult = history) {
                        is HistoryState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is HistoryState.Success -> {
                            Text(
                                mokoString(MR.strings.history),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            if (historyResult.items.isEmpty()) {
                                Text(
                                    text = mokoString(MR.strings.no_history),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                historyResult.items.forEach { historyItem ->
                                    HistoryItemView(historyItem)
                                }
                            }
                        }

                        is HistoryState.Error -> {}
                        else -> {}
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text(mokoString(MR.strings.are_you_sure)) },
                    text = { Text(mokoString(MR.strings.book_delete_message)) },
                    dismissButton = {
                        TextButton(
                            onClick = { confirmDelete = false },
                        ) { Text(mokoString(MR.strings.cancel)) }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirmDelete = false
                                viewModel.deleteBook()
                            },
                        ) { Text(mokoString(MR.strings.yes)) }
                    },
                )
            }
        }
    }
}

@Composable
fun BookFileCard(file: BookFile) {
    ContainerCard {
        Text(
            text = file.path?.breakable() ?: "",
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text =
                listOfNotNull(
                    file.quality?.qualityLabel,
                    file.size?.bytesAsFileSizeString(),
                ).joinToString(BULLET),
            fontSize = 12.sp,
        )
        file.dateAdded?.format("MMM d, yyyy")?.let { formattedDate ->
            Text(
                text = mokoString(MR.strings.add, formattedDate),
                fontSize = 12.sp,
            )
        }
    }
}
