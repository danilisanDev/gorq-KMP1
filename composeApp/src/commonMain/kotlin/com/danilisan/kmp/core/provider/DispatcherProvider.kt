package com.danilisan.kmp.core.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val immediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class ProductionDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val immediate = Dispatchers.Main.immediate
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
    override val unconfined = Dispatchers.Unconfined
}

class TestDispatcherProvider(scheduler: TestCoroutineScheduler? = null) : DispatcherProvider {
    override val main = StandardTestDispatcher(scheduler)
    override val immediate = StandardTestDispatcher(scheduler)
    override val io = StandardTestDispatcher(scheduler)
    override val default = StandardTestDispatcher(scheduler)
    @OptIn(ExperimentalCoroutinesApi::class)
    override val unconfined = UnconfinedTestDispatcher(scheduler)
}