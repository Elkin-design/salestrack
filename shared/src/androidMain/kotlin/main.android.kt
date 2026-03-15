package com.salestrack.util

import androidx.compose.runtime.Composable
import com.salestrack.presentation.App

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() = App()
