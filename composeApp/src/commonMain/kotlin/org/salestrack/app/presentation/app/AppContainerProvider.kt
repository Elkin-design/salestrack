package org.salestrack.app.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.mp.KoinPlatform

@Composable
fun rememberAppContainer(): AppContainer {
    return remember {
        KoinPlatform.getKoin().get<AppContainer>()
    }
}
