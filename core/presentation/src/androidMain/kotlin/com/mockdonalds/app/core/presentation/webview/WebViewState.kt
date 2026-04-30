package com.mockdonalds.app.core.presentation.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class WebViewState internal constructor() {
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    internal var goBackHandler: () -> Unit = {}

    fun goBack() {
        if (canGoBack) goBackHandler()
    }
}

@Composable
fun rememberWebViewState(): WebViewState = remember { WebViewState() }
