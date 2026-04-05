package org.salestrack.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.salestrack.app.core.designsystem.SalesTrackTheme
import org.salestrack.app.presentation.app.AppNavHost
import org.salestrack.app.presentation.app.di.startAppKoinIfNeeded

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    remember { startAppKoinIfNeeded() }
    SalesTrackTheme {
        AppNavHost(modifier = modifier)
    }
}