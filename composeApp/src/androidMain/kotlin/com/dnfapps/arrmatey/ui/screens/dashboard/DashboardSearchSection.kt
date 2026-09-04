package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SearchResultList
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardSearchSection(
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = koinViewModel(),
    onItemClick: (SearchResult) -> Unit = {},
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchShowBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()
    val searchShowInstanceIndicatorShadow by viewModel.searchShowInstanceIndicatorShadow.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState(searchQuery)

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            OutlinedTextField(
                state = textFieldState,
                placeholder = { Text(mokoString(MR.strings.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textFieldState.text.isNotEmpty()) {
                        IconButton(onClick = { textFieldState.clearText() }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(visible = textFieldState.text.isNotEmpty()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                ) {
                    if (isSearching && searchState.isEmpty()) {
                        LoadingIndicator(
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(8.dp),
                        )
                    } else if (searchState.isNotEmpty()) {
                        SearchResultList(
                            items = searchState,
                            onItemClick = onItemClick,
                            includeOverview = true,
                            showBanners = searchShowBanners,
                            showInstanceIndicatorShadow = searchShowInstanceIndicatorShadow,
                            modifier = Modifier.heightIn(max = 400.dp),
                        )
                    }
                }
            }
        }
    }
}
