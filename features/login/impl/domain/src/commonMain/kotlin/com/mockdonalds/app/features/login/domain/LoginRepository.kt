package com.mockdonalds.app.features.login.domain

import com.mockdonalds.app.features.login.api.domain.LoginContent
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun getLoginContent(): Flow<LoginContent>
}
