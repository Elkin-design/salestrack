import com.salestrack.di.initKoin
import com.salestrack.presentation.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    initKoin()
    application {
        Window(onCloseRequest = ::exitApplication, title = "SalesTrack Desktop") {
            App()
        }
    }
}