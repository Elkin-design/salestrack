package com.salestrack.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import com.salestrack.presentation.App

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController { App() }