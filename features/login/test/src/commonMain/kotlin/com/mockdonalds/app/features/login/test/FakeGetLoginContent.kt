package com.mockdonalds.app.features.login.test

import com.mockdonalds.app.features.login.api.domain.GetLoginContent
import com.mockdonalds.app.features.login.api.domain.LoginContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetLoginContent(
    initial: LoginContent = DEFAULT,
) : GetLoginContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<LoginContent> = _content

    fun emit(content: LoginContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = LoginContent(
            logoUrl = "",
        )
    }
}
