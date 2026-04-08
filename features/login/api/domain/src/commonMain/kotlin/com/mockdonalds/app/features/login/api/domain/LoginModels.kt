package com.mockdonalds.app.features.login.api.domain

data class LoginResult(
    val success: Boolean,
    val errorMessage: String? = null,
)

data class LoginContent(
    val logoUrl: String,
)
