package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.autoSaver
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
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.isToday
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import kotlin.math.exp

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
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.books()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .weight(1.5f)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Books (${books.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Series (${series.size})") }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = mokoString(MR.strings.history),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navigation.navigateTo(ArrScreen.AuthorFiles(author))
                }
            )
        }
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                expandVertically()
                    .togetherWith(shrinkVertically())
            }
        ) { tabIndex ->
            when (tabIndex) {
                0 -> BooksView(
                    author = author,
                    files = files,
                    books = books,
                    searchIds = searchIds,
                    onToggleMonitor = onToggleMonitor,
                    onAutomaticSearch = onAutomaticSearch
                )

                1 -> SeriesView(
                    series = series,
                    files = files,
                    books = books,
                    searchIds = searchIds,
                    onToggleMonitor = onToggleMonitor,
                    onToggleSeriesMonitor = onToggleSeriesMonitor,
                    onAutomaticSearch = onAutomaticSearch
                )
            }
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
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.books()
) {
    Column {
        books.forEach { book ->
            BookRow(
                book = book,
                bookFile = files.firstOrNull { it.bookId == book.id },
                isActive = false,
                onAutomaticSearch = onAutomaticSearch,
                onToggleMonitor = onToggleMonitor,
                searchInProgress = { searchIds.contains(it) },
                onClick = {
                    navigation.navigateTo(ArrScreen.BookDetails(author, book))
                }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    progressLabel: String? = null,
    seriesPosition: String? = null,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.books()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val titleString = buildAnnotatedString {
                seriesPosition?.let { seriesPosition ->
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("${seriesPosition}. ")
                    }
                }
                append(book.title)
            }
            Text(titleString)

            val releaseDate = book.releaseDate?.takeIf { it.isTodayOrAfter() }
            val (statusText, statusColor) = when {
                isActive && progressLabel != null -> progressLabel to ArrLightPurple
                bookFile?.quality != null -> bookFile.fileQualityName!! to MaterialTheme.colorScheme.tertiary
                releaseDate != null -> mokoString(MR.strings.unaired) to Color.Unspecified
                else -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = statusColor,
                    fontStyle = if (statusColor != Color.Unspecified) FontStyle.Italic else FontStyle.Normal
                )

                val (weight, color) = if (book.releaseDate?.isToday() == true)
                    FontWeight.Medium to MaterialTheme.colorScheme.primary
                else
                    FontWeight.Normal to Color.Unspecified
                Text(
                    text = "$Bullet${book.releaseDate?.format("MMM d, yyyy")}",
                    color = color,
                    fontWeight = weight,
                    fontSize = 14.sp
                )
            }
        }

        IconButton(
            onClick = {
                val destination = ArrScreen.BookRelease(bookId = book.id)
                navigation.navigateTo(destination)
            },
            modifier = Modifier.size(24.dp),
            enabled = book.monitored
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
            modifier = Modifier.size(24.dp)
        ) {
            if (searchInProgress(book.id)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
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
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (book.monitored) {
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
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.books()
) {
    Column {
        series.forEach { bookSeries ->
            val seriesBooks = remember(series, books) {
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
                label = "iconRotation"
            )
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContainerCard(modifier = Modifier.clickable {
                    expanded = !expanded
                }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bookSeries.title ?: mokoString(MR.strings.unknown),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "${bookSeries.links.size} books",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ExpandCircleDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Icon(
                            imageVector = if (wholeSeriesMonitored) {
                                Icons.Default.Bookmark
                            } else Icons.Default.BookmarkBorder,
                            contentDescription = if (wholeSeriesMonitored) {
                                mokoString(MR.strings.monitored)
                            } else {
                                mokoString(MR.strings.unmonitored)
                            },
                            modifier = Modifier.clickable {
                                onToggleSeriesMonitor(seriesBooks)
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        bookSeries.links.sortedBy { it.position }.forEach { link ->
                            seriesBooks.firstOrNull { it.id == link.bookId }?.let { book ->
                                BookRow(
                                    book = book,
                                    bookFile = files.firstOrNull { it.bookId == link.bookId },
                                    isActive = false,
                                    onAutomaticSearch = onAutomaticSearch,
                                    onToggleMonitor = onToggleMonitor,
                                    searchInProgress = { searchIds.contains(it) },
                                    onClick = {
                                        navigation.navigateTo(ArrScreen.BookRelease(book.id))
                                    },
                                    seriesPosition = link.position
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