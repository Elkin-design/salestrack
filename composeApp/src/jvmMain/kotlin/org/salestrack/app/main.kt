package org.salestrack.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.salestrack.app.presentation.app.di.startAppKoinIfNeeded

fun main() {
    startAppKoinIfNeeded()
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SalesTrack",
        ) {
            App()
        }
    }
}