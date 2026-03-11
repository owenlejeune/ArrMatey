package com.dnfapps.arrmatey.ui.components.appbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.AppBarWithSearchColors
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import com.dnfapps.arrmatey.entensions.isCollapsed
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenSearchAppBar(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    searchBarState: SearchBarState = rememberSearchBarState(),
    searchPlaceholder: String = mokoString(MR.strings.search),
    scrollBehavior: SearchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior(),
    colors: AppBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
    ),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    leadingIcon: @Composable () -> Unit = { Icon(Icons.Default.Search, null) },
    trailingIcon: @Composable () -> Unit = {},
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                colors = colors.searchBarColors.inputFieldColors,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = {
                    Text(
                        modifier = Modifier.clearAndSetSemantics {},
                        text = searchPlaceholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    if (searchBarState.isExpanded()) {
                        IconButton(onClick = { scope.launch { searchBarState.animateToCollapsed() } }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    } else {
                        leadingIcon()
                    }
                },
                trailingIcon = {
                    AnimatedContent(
                        targetState = textFieldState.text.isNotEmpty()
                    ) { isNotEmpty ->
                        if (isNotEmpty) {
                            IconButton(onClick = { textFieldState.clearText() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        } else {
                            trailingIcon()
                        }
                    }
                }
            )
        }

    AppBarWithSearch(
        state = searchBarState,
        scrollBehavior = scrollBehavior,
        colors = colors,
        inputField = inputField,
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            AnimatedVisibility(
                visible = searchBarState.isCollapsed(),
                enter = expandIn(),
                exit = shrinkOut(),
                content = { navigationIcon() }
            )
        },
        actions = {
            AnimatedVisibility(
                visible = searchBarState.isCollapsed(),
                enter = expandIn(),
                exit = shrinkOut()
            ) {
                Row { actions() }
            }
        }
    )
    ExpandedFullScreenContainedSearchBar(
        state = searchBarState,
        inputField = inputField,
        colors = colors.searchBarColors,
    ) {
        expandedContent()
    }
}