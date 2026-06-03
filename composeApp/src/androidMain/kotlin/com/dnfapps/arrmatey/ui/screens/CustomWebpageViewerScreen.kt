package com.dnfapps.arrmatey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageViewerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomWebpageViewerScreen(
    webpageId: Long,
    wideRailIsVisible: Boolean,
    customWebpageViewModel: CustomWebpageViewerViewModel = koinInjectParams(webpageId)
) {
    val webpage by customWebpageViewModel.webpage.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var progress by remember { mutableFloatStateOf(0f) }
    var currentUrl by remember { mutableStateOf("") }
    var currentTitle by remember { mutableStateOf("") }

    val lifecyclerOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecyclerOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webView?.onPause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    webView?.onResume()
                }

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
                            text = currentTitle.ifEmpty { webpage?.name ?: "" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentUrl.isNotEmpty()) {
                            Text(
                                text = currentUrl,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                    Box {
                        IconButton(onClick = {
                            menuExpanded = true
                        }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenuPopup(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.refresh)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(0, 4),
                                    leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                    onClick = {
                                        menuExpanded = false
                                        webView?.reload()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.share)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(1, 4),
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = {
                                        menuExpanded = false
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, currentUrl.ifEmpty { webpage?.url ?: "" })
                                        }
                                        context.startActivity(Intent.createChooser(intent, null))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.open_in_browser)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(2, 5),
                                    leadingIcon = { Icon(Icons.Default.OpenInBrowser, null) },
                                    onClick = {
                                        menuExpanded = false
                                        context.openLink(currentUrl.ifEmpty { webpage?.url ?: "" })
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.copy_link)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(3, 5),
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        menuExpanded = false
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("URL", currentUrl.ifEmpty { webpage?.url ?: "" })
                                        clipboard.setPrimaryClip(clip)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.replace_url_with_current)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(4, 5),
                                    leadingIcon = { Icon(Icons.Default.Link, null) },
                                    onClick = {
                                        menuExpanded = false
                                        customWebpageViewModel.updateUrl(currentUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            webpage?.let { webpage ->
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun doUpdateVisitedHistory(
                                    view: WebView?,
                                    url: String?,
                                    isReload: Boolean
                                ) {
                                    super.doUpdateVisitedHistory(view, url, isReload)
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                    currentUrl = url ?: ""
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress / 100f
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    currentTitle = title ?: ""
                                }
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            val headersMap = webpage.headers.associate { it.key to it.value }
                            loadUrl(webpage.url, headersMap)
                        }.also { webView = it }
                    },
                    update = { view ->
                        canGoBack = view.canGoBack()
                        canGoForward = view.canGoForward()
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { view ->
                        view.stopLoading()
                        view.loadUrl("about:blank")
                        view.clearHistory()
                        view.clearCache(true)
                        view.removeAllViews()
                        view.destroyDrawingCache()
                        view.destroy()
                    }
                )
            }

            if (progress > 0f && progress < 1f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
