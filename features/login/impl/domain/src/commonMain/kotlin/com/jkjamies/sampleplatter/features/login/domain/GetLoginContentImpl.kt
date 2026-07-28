package com.jkjamies.sampleplatter.features.login.domain

import com.jkjamies.sampleplatter.features.login.api.domain.GetLoginContent
import com.jkjamies.sampleplatter.features.login.api.domain.LoginContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class GetLoginContentImpl(
    private val repository: LoginRepository,
) : GetLoginContent() {
    override fun createObservable(params: Unit): Flow<LoginContent> {
        return repository.getLoginContent()
    }
}
