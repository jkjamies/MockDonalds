package com.jkjamies.sampleplatter.core.presentation.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.jkjamies.sampleplatter.core.presentation.webview.internal.SamplePlatterWebViewClient
import com.jkjamies.sampleplatter.core.presentation.webview.internal.WebViewErrorView
import com.jkjamies.sampleplatter.core.presentation.webview.internal.WebViewLoadingIndicator

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent(
    url: String,
    modifier: Modifier = Modifier,
    allowJs: Boolean = false,
    onExternalLink: ((String) -> Boolean)? = null,
    onError: ((WebViewError) -> Unit)? = null,
    state: WebViewState = rememberWebViewState(),
    errorContent: (@Composable (WebViewError, retry: () -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val initialUri = remember(url) { Uri.parse(url) }
    val currentOnExternalLink by rememberUpdatedState(onExternalLink)
    val currentOnError by rememberUpdatedState(onError)

    var isLoading by remember { mutableStateOf(true) }
    var terminalError by remember { mutableStateOf<WebViewError?>(null) }
    var pendingRetry by remember { mutableStateOf(0) }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = allowJs
            settings.domStorageEnabled = allowJs
            webViewClient = SamplePlatterWebViewClient(
                initialHost = initialUri.host,
                initialScheme = initialUri.scheme,
                context = context,
                onExternalLink = { link -> currentOnExternalLink?.invoke(link) == true },
                onError = { err -> currentOnError?.invoke(err) },
                onLoadingStateChanged = { isLoading = it },
                onTerminalErrorChanged = { terminalError = it },
                onNavigationStateChanged = { canGoBack -> state.canGoBack = canGoBack },
            )
        }
    }

    LaunchedEffect(webView) {
        state.goBackHandler = { webView.goBack() }
    }

    LaunchedEffect(url, pendingRetry) {
        webView.loadUrl(url)
    }

    DisposableEffect(webView, initialUri.host) {
        onDispose {
            webView.stopLoading()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webView.destroy()
        }
    }

    Box(modifier = modifier) {
        if (terminalError == null) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize(),
            )
            if (isLoading) {
                WebViewLoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        } else {
            val retry: () -> Unit = {
                terminalError = null
                pendingRetry += 1
            }
            val effectiveErrorContent = errorContent ?: { e, r -> WebViewErrorView(e, r) }
            effectiveErrorContent(terminalError as WebViewError, retry)
        }
    }
}
