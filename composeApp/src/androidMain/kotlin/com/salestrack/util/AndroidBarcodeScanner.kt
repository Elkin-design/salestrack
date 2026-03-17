package com.salestrack.util

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class AndroidBarcodeScanner(private val context: Context) : BarcodeScanner {
    
    // In a real KMP app with multiple screens, we'd use a more robust way to get the Activity.
    // For this implementation, we'll assume the context is an Activity or can start one.
    
    override fun startScan(onResult: (String?) -> Unit) {
        // Since we are in a shared module and don't want to overcomplicate the Activity result handling here,
        // we'll use a simple approach: Launch a dedicated Activity and use a static callback.
        BarcodeScannerActivity.onBarcodeScanned = onResult
        val intent = Intent(context, BarcodeScannerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
