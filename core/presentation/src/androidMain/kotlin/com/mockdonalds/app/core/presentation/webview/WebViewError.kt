package com.mockdonalds.app.core.presentation.webview

data class WebViewError(
    val url: String,
    val code: Int,
    val description: String,
)
