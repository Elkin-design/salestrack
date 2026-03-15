package com.salestrack.app

import com.salestrack.di.initKoin
import com.salestrack.presentation.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start Koin for Android
        initKoin {
            androidContext(this@MainActivity)
        }

        setContent {
            App()
        }
    }
}
