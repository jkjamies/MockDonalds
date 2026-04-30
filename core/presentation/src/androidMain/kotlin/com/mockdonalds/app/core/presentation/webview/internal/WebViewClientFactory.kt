package com.mockdonalds.app.core.presentation.webview.internal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import com.mockdonalds.app.core.presentation.webview.WebViewError

internal fun interface NavigationStateUpdate {
    fun onNavigationStateChanged(canGoBack: Boolean)
}

internal class MockDonaldsWebViewClient(
    private val initialHost: String?,
    private val initialScheme: String?,
    private val context: Context,
    private val onExternalLink: ((String) -> Boolean)?,
    private val onError: ((WebViewError) -> Unit)?,
    private val onLoadingStateChanged: (Boolean) -> Unit,
    private val onTerminalErrorChanged: (WebViewError?) -> Unit,
    private val onNavigationStateChanged: NavigationStateUpdate,
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        onLoadingStateChanged(true)
        onTerminalErrorChanged(null)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        onLoadingStateChanged(false)
        onNavigationStateChanged.onNavigationStateChanged(view?.canGoBack() == true)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val targetUri = request?.url ?: return false
        val isOffHost = initialHost != null &&
            (targetUri.host != initialHost || targetUri.scheme != initialScheme)
        if (!isOffHost) return false

        val handled = onExternalLink?.invoke(targetUri.toString()) == true
        if (handled) return true

        openExternally(targetUri)
        return true
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        val isMainFrame = request?.isForMainFrame == true
        if (!isMainFrame) return

        val webViewError = WebViewError(
            url = request.url?.toString().orEmpty(),
            code = error?.errorCode ?: -1,
            description = error?.description?.toString().orEmpty(),
        )
        onTerminalErrorChanged(webViewError)
        onError?.invoke(webViewError)
        onLoadingStateChanged(false)
    }

    private fun openExternally(uri: Uri) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (_: ActivityNotFoundException) {
            val fallback = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (_: ActivityNotFoundException) {
                // No browser installed; swallow — there is nothing useful to do.
            }
        }
    }
}
