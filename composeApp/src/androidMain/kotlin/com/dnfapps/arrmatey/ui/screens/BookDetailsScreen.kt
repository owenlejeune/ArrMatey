package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.viewmodel.BookDetailsViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DetailHeaderBanner
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.ReleaseDownloadButtons
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@Composable
fun BookDetailsScreen(
    book: Book,
    author: Author,
    viewModel: BookDetailsViewModel = koinInjectParams(author.id, book),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.books()
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
                        onClick = { navigation.popBackStack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMonitor() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = if (currentBook.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = { confirmDelete = true },
                        colors = IconButtonDefaults.headerBarColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = bookFiles.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues.copy(top = 0.dp, bottom = 0.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BookDetailsHeader(currentBook, author)

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bookEdition?.overview?.let { overview ->
                        ItemDescriptionCard(overview)
                    }

                    ReleaseDownloadButtons(
                        onInteractiveClicked = {
                            val destination = ArrScreen.BookRelease(bookId = currentBook.id)
                            navigation.navigateTo(destination)
                        },
                        onAutomaticClicked = {
                            viewModel.executeAutomaticSearch()
                        },
                        automaticSearchEnabled = currentBook.monitored
                    )

                    Text(
                        text = mokoString(MR.strings.files),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                    bookFiles.forEach { file ->
                        BookFileCard(file)
                    }

                    when (val historyResult = history) {
                        is HistoryState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is HistoryState.Success -> {
                            Text(
                                mokoString(MR.strings.history),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (historyResult.items.isEmpty()) {
                                Text(
                                    text = mokoString(MR.strings.no_history),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
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
                            onClick = { confirmDelete = false }
                        ) { Text(mokoString(MR.strings.cancel)) }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirmDelete = false
                                viewModel.deleteBook()
                            }
                        ) { Text(mokoString(MR.strings.yes)) }
                    }
                )
            }
        }
    }
}

@Composable
fun BookDetailsHeader(
    book: Book,
    author: Author
) {
    Box(modifier = Modifier.fillMaxSize()) {
        DetailHeaderBanner(book.getCover()?.remoteUrl)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 170.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BookCover(book)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title.breakable(),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 1.em,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 38.sp,
                        stepSize = 2.sp
                    )
                )
                book.seriesTitle?.let { seriesTitle ->
                    Text(
                        text = seriesTitle
                    )
                }
                Text(
                    text = buildString {
                        author.title?.let { authorName ->
                            append(authorName)
                            append(Bullet)
                        }
                        book.pageCount?.let { pageCount ->
                            append("$pageCount pages")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BookCover(
    book: Book
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier
            .height(220.dp)
            .aspectRatio(AspectRatio.Poster.ratio, true)
    ) {
        AsyncImage(
            model = rememberRemoteImageData(
                url = book.getCover()?.remoteUrl
            ),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BookFileCard(file: BookFile) {
    ContainerCard {
        Text(
            text = file.path?.breakable() ?: "",
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = listOfNotNull(
                file.quality?.qualityLabel,
                file.size?.bytesAsFileSizeString()
            ).joinToString(Bullet),
            fontSize = 12.sp
        )
        file.dateAdded?.format("MMM d, yyyy")?.let { formattedDate ->
            Text(
                text = mokoString(MR.strings.add, formattedDate),
                fontSize = 12.sp
            )
        }
    }
}