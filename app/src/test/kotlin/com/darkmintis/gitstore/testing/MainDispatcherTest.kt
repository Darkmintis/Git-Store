package com.darkmintis.gitstore.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
open class MainDispatcherTest {
    protected lateinit var testDispatcher: TestDispatcher

    protected fun runViewModelTest(
        testBody: suspend TestScope.() -> Unit
    ) {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            runTest(context = testDispatcher, testBody = testBody)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
