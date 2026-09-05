package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebViewScreen(
    url: String,
    title: String? = null,
    bannerMessage: String? = null,
    onBannerClick: (() -> Unit)? = null,
    wideRailIsVisible: Boolean,
    onBack: (() -> Unit)? = null,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showBanner by remember { mutableStateOf(bannerMessage != null) }

    var progress by remember { mutableFloatStateOf(0f) }
    var currentUrl by remember { mutableStateOf(url) }
    var currentTitle by remember { mutableStateOf(title ?: "") }

    val lifecyclerOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecyclerOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                    Lifecycle.Event.ON_RESUME -> webView?.onResume()
                    Lifecycle.Event.ON_DESTROY -> {
                        webView?.apply {
                            stopLoading()
                            loadUrl("about:blank")
                            clearHistory()
                            clearCache(true)
                            destroy()
                        }
                        webView = null
                    }
                    else -> {}
                }
            }
        lifecyclerOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecyclerOwner.lifecycle.removeObserver(observer)
            webView?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                clearCache(true)
                removeAllViews()
                destroyDrawingCache()
                destroy()
            }
            webView = null
        }
    }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentTitle.ifEmpty { title ?: "" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (currentUrl.isNotEmpty()) {
                            Text(
                                text = currentUrl,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                        }
                    } else if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = canGoBack,
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = canGoForward,
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenuPopup(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1),
                            ) {
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.refresh)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(0, 1),
                                    leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                    onClick = {
                                        menuExpanded = false
                                        webView?.reload()
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            AnimatedVisibility(
                visible = showBanner && bannerMessage != null,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable(enabled = onBannerClick != null) { onBannerClick?.invoke() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = bannerMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showBanner = false }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        WebView(context)
                            .apply {
                                webViewClient =
                                    object : WebViewClient() {
                                        override fun doUpdateVisitedHistory(
                                            view: WebView?,
                                            url: String?,
                                            isReload: Boolean,
                                        ) {
                                            super.doUpdateVisitedHistory(view, url, isReload)
                                            canGoBack = view?.canGoBack() == true
                                            canGoForward = view?.canGoForward() == true
                                            currentUrl = url ?: ""
                                        }
                                    }

                                webChromeClient =
                                    object : WebChromeClient() {
                                        override fun onProgressChanged(
                                            view: WebView?,
                                            newProgress: Int,
                                        ) {
                                            progress = newProgress / 100f
                                        }

                                        override fun onReceivedTitle(
                                            view: WebView?,
                                            title: String?,
                                        ) {
                                            currentTitle = title ?: ""
                                        }
                                    }

                                @SuppressLint("SetJavaScriptEnabled")
                                settings.apply {
                                    // JavaScript is required to render external web pages (e.g., TMDB)
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                }

                                loadUrl(url)
                            }.also { webView = it }
                    },
                    update = { view ->
                        canGoBack = view.canGoBack()
                        canGoForward = view.canGoForward()
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                if (progress > 0f && progress < 1f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}
