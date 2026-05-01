package com.dnfapps.arrmatey.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BooksArea(
    author: Author,
    series: List<BookSeries>,
    files: List<BookFile>,
    searchIds: Set<Long>
) {
    Text(
        text = "TODO",
        style = MaterialTheme.typography.headlineLargeEmphasized,
        color = Color.Red
    )
}