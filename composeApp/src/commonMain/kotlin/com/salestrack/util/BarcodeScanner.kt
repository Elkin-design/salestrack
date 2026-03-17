package com.salestrack.util

interface BarcodeScanner {
    fun startScan(onResult: (String?) -> Unit)
}
