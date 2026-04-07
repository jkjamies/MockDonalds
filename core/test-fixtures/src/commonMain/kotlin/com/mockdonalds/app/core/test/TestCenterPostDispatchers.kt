package com.mockdonalds.app.core.test

import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestCenterPostDispatchers(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : CenterPostDispatchers {
    override val default: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
}
