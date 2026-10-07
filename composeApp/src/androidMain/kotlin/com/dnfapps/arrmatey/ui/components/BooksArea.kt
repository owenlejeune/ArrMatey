package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReadarrQueueItem
import com.dnfapps.arrmatey.compose.utils.BookMediaFilterBy
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.extensions.isToday
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BooksArea(
    author: Author,
    series: List<BookSeries>,
    files: List<BookFile>,
    books: List<Book>,
    searchIds: Set<Long>,
    onToggleMonitor: (Book) -> Unit,
    onToggleSeriesMonitor: (List<Book>) -> Unit,
    onAutomaticSearch: (Long) -> Unit,
    onNavigateToBookDetails: (Author, Book) -> Unit,
    onNavigateToBookRelease: (Long) -> Unit,
    modifier: Modifier = Modifier,
    queueItems: List<QueueItem> = emptyList(),
    selectedMediaTypeFilter: BookMediaFilterBy = BookMediaFilterBy.All,
    onSelectMediaTypeFilter: (BookMediaFilterBy) -> Unit = {},
    onEditAuthor: (() -> Unit)? = null,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val isChaptarr = remember(author, books) {
        author.isChaptarr || books.any { it.mediaType != null }
    }

    val hasMixedMediaTypes = remember(books, author, isChaptarr) {
        author.hasMixedMediaTypes(books)
    }

    val hasAudiobooksConfigured = remember(author) { author.hasAudiobookConfigured }
    val hasEbooksConfigured = remember(author) { author.hasEbookConfigured }

    val ebookCount = remember(books) {
        books.count { it.mediaType == null || it.mediaType == BookMediaType.EBook }
    }
    val audiobookCount = remember(books) {
        books.count { it.mediaType == BookMediaType.Audiobook }
    }

    val filteredBooks = remember(books, selectedMediaTypeFilter, hasMixedMediaTypes) {
        if (!hasMixedMediaTypes || selectedMediaTypeFilter == BookMediaFilterBy.All) {
            books
        } else if (selectedMediaTypeFilter == BookMediaFilterBy.Audiobook) {
            books.filter { it.mediaType == BookMediaType.Audiobook }
        } else {
            books.filter { it.mediaType == null || it.mediaType == BookMediaType.EBook }
        }
    }

    val isUnconfiguredAudiobook = selectedMediaTypeFilter == BookMediaFilterBy.Audiobook && !hasAudiobooksConfigured
    val isUnconfiguredEbook = selectedMediaTypeFilter == BookMediaFilterBy.EBook && !hasEbooksConfigured

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SegmentedButton(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text(mokoString(MR.strings.books_area_books_tab, filteredBooks.size)) },
            )
            SegmentedButton(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text(mokoString(MR.strings.books_area_series_tab, series.size)) },
            )
        }

        if (hasMixedMediaTypes && selectedTabIndex == 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = selectedMediaTypeFilter == BookMediaFilterBy.All,
                    onClick = { onSelectMediaTypeFilter(BookMediaFilterBy.All) },
                    label = { Text(mokoString(BookMediaFilterBy.All.resource) + " (${books.size})") },
                )
                FilterChip(
                    selected = selectedMediaTypeFilter == BookMediaFilterBy.EBook,
                    onClick = { onSelectMediaTypeFilter(BookMediaFilterBy.EBook) },
                    label = { Text(mokoString(BookMediaFilterBy.EBook.resource) + " ($ebookCount)") },
                )
                FilterChip(
                    selected = selectedMediaTypeFilter == BookMediaFilterBy.Audiobook,
                    onClick = { onSelectMediaTypeFilter(BookMediaFilterBy.Audiobook) },
                    label = { Text(mokoString(BookMediaFilterBy.Audiobook.resource) + " ($audiobookCount)") },
                )
            }
        }

        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            },
        ) { tabIndex ->
            when {
                isUnconfiguredAudiobook -> {
                    UnconfiguredTypeNotice(
                        message = mokoString(MR.strings.no_audiobook_root_folder_configured),
                        modifier = Modifier.padding(vertical = 8.dp),
                        onClick = onEditAuthor,
                    )
                }

                isUnconfiguredEbook -> {
                    UnconfiguredTypeNotice(
                        message = mokoString(MR.strings.no_ebook_root_folder_configured),
                        modifier = Modifier.padding(vertical = 8.dp),
                        onClick = onEditAuthor,
                    )
                }

                tabIndex == 0 -> {
                    BooksView(
                        author = author,
                        files = files,
                        books = filteredBooks,
                        searchIds = searchIds,
                        onToggleMonitor = onToggleMonitor,
                        onAutomaticSearch = onAutomaticSearch,
                        onNavigateToBookDetails = onNavigateToBookDetails,
                        onNavigateToBookRelease = onNavigateToBookRelease,
                        queueItems = queueItems,
                    )
                }

                else -> {
                    SeriesView(
                        series = series,
                        files = files,
                        books = filteredBooks,
                        searchIds = searchIds,
                        onToggleMonitor = onToggleMonitor,
                        onToggleSeriesMonitor = onToggleSeriesMonitor,
                        onAutomaticSearch = onAutomaticSearch,
                        onNavigateToBookRelease = onNavigateToBookRelease,
                        queueItems = queueItems,
                    )
                }
            }
        }
    }
}

@Composable
private fun UnconfiguredTypeNotice(
    message: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    ContainerCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BooksView(
    author: Author,
    files: List<BookFile>,
    books: List<Book>,
    searchIds: Set<Long>,
    onToggleMonitor: (Book) -> Unit,
    onAutomaticSearch: (Long) -> Unit,
    onNavigateToBookDetails: (Author, Book) -> Unit,
    onNavigateToBookRelease: (Long) -> Unit,
    queueItems: List<QueueItem> = emptyList(),
) {
    Column {
        books.forEach { book ->
            val activeQueueItem = queueItems.filterIsInstance<ReadarrQueueItem>()
                .firstOrNull { it.bookId == book.id || it.book?.id == book.id }
            BookRow(
                book = book,
                bookFile = files.firstOrNull { it.bookId == book.id },
                isActive = activeQueueItem != null,
                progressLabel = activeQueueItem?.progressLabel,
                onAutomaticSearch = onAutomaticSearch,
                onToggleMonitor = onToggleMonitor,
                searchInProgress = { searchIds.contains(it) },
                onNavigateToBookRelease = onNavigateToBookRelease,
                onClick = {
                    onNavigateToBookDetails(author, book)
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun BookRow(
    book: Book,
    bookFile: BookFile?,
    isActive: Boolean,
    onAutomaticSearch: (Long) -> Unit,
    onToggleMonitor: (Book) -> Unit,
    searchInProgress: (Long) -> Boolean,
    onNavigateToBookRelease: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    progressLabel: String? = null,
    seriesPosition: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val titleString =
                buildAnnotatedString {
                    seriesPosition?.let { seriesPosition ->
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("$seriesPosition. ")
                        }
                    }
                    append(book.title)
                }
            Text(
                text = titleString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            if (book.narratorNames.isNotEmpty()) {
                Text(
                    text = mokoString(MR.strings.narrated_by, book.narratorNames.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }

            val releaseDate = book.releaseDate?.takeIf { it.isTodayOrAfter() }
            val (statusText, statusColor) =
                when {
                    isActive && progressLabel != null -> progressLabel to ArrLightPurple
                    bookFile?.quality != null -> bookFile.fileQualityName!! to MaterialTheme.colorScheme.tertiary
                    releaseDate != null -> mokoString(MR.strings.unaired) to Color.Unspecified
                    else -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
                }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (book.mediaType != null) {
                    val isAudiobook = book.mediaType == BookMediaType.Audiobook
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = if (isAudiobook) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = mokoString(book.mediaType!!.resource),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAudiobook) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontStyle = if (statusColor != Color.Unspecified) FontStyle.Italic else FontStyle.Normal,
                )

                val (weight, color) =
                    if (book.releaseDate?.isToday() == true) {
                        FontWeight.Medium to MaterialTheme.colorScheme.primary
                    } else {
                        FontWeight.Normal to Color.Unspecified
                    }
                Text(
                    text = "$BULLET${book.releaseDate?.format("MMM d, yyyy")}",
                    color = color,
                    fontWeight = weight,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        IconButton(
            onClick = {
                onNavigateToBookRelease(book.id)
            },
            modifier = Modifier.size(24.dp),
            enabled = book.monitored,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onAutomaticSearch(book.id)
            },
            enabled = book.monitored && !searchInProgress(book.id),
            modifier = Modifier.size(24.dp),
        ) {
            if (searchInProgress(book.id)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            }
        }
        IconButton(
            onClick = {
                onToggleMonitor(book)
            },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector =
                if (book.monitored) {
                    Icons.Default.Bookmark
                } else {
                    Icons.Default.BookmarkBorder
                },
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SeriesView(
    series: List<BookSeries>,
    files: List<BookFile>,
    books: List<Book>,
    searchIds: Set<Long>,
    onToggleMonitor: (Book) -> Unit,
    onToggleSeriesMonitor: (List<Book>) -> Unit,
    onAutomaticSearch: (Long) -> Unit,
    onNavigateToBookRelease: (Long) -> Unit,
    queueItems: List<QueueItem> = emptyList(),
) {
    Column {
        series.forEach { bookSeries ->
            val seriesBooks =
                remember(series, books) {
                    bookSeries.links.mapNotNull { link ->
                        books.firstOrNull { it.id == link.bookId }
                    }
                }
            val monitoredBooksCount by remember {
                derivedStateOf { seriesBooks.count { it.monitored } }
            }
            val wholeSeriesMonitored by remember {
                derivedStateOf { monitoredBooksCount == bookSeries.links.size }
            }

            var expanded by rememberSaveable { mutableStateOf(true) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation",
            )
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ContainerCard(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = MaterialTheme.shapes.large,
                    modifier =
                    Modifier.clickable {
                        expanded = !expanded
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = bookSeries.title ?: mokoString(MR.strings.unknown),
                                style = MaterialTheme.typography.titleLargeEmphasized,
                            )
                            Text(
                                text = "${bookSeries.links.size} books",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ExpandCircleDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(iconRotation),
                        )
                        Icon(
                            imageVector =
                            if (wholeSeriesMonitored) {
                                Icons.Default.Bookmark
                            } else {
                                Icons.Default.BookmarkBorder
                            },
                            contentDescription =
                            if (wholeSeriesMonitored) {
                                mokoString(MR.strings.monitored)
                            } else {
                                mokoString(MR.strings.unmonitored)
                            },
                            modifier =
                            Modifier.clickable {
                                onToggleSeriesMonitor(seriesBooks)
                            },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        bookSeries.links.sortedBy { it.position }.forEach { link ->
                            seriesBooks.firstOrNull { it.id == link.bookId }?.let { book ->
                                val activeQueueItem = queueItems.filterIsInstance<ReadarrQueueItem>()
                                    .firstOrNull { it.bookId == book.id || it.book?.id == book.id }
                                BookRow(
                                    book = book,
                                    bookFile = files.firstOrNull { it.bookId == link.bookId },
                                    isActive = activeQueueItem != null,
                                    progressLabel = activeQueueItem?.progressLabel,
                                    onAutomaticSearch = onAutomaticSearch,
                                    onToggleMonitor = onToggleMonitor,
                                    searchInProgress = { searchIds.contains(it) },
                                    onNavigateToBookRelease = onNavigateToBookRelease,
                                    onClick = {
                                        onNavigateToBookRelease(book.id)
                                    },
                                    seriesPosition = link.position,
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
