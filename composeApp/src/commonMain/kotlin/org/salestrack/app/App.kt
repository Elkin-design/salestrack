package org.salestrack.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.salestrack.app.core.designsystem.SalesTrackTheme
import org.salestrack.app.presentation.app.AppNavHost

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    SalesTrackTheme {
        AppNavHost(modifier = modifier)
    }
}