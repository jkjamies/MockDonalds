package com.mockdonalds.app.features.mobile.login.domain

import com.mockdonalds.app.features.mobile.login.api.domain.LoginContent
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun getLoginContent(): Flow<LoginContent>
}
