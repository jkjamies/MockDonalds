package com.jkjamies.sampleplatter.features.login.domain

import com.jkjamies.sampleplatter.features.login.api.domain.LoginContent
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun getLoginContent(): Flow<LoginContent>
}
