package com.salestrack.util

class DesktopBarcodeScanner : BarcodeScanner {
    override fun startScan(onResult: (String?) -> Unit) {
        // Desktop scanners usually act as a keyboard (HID).
        // This interface method is less used on Desktop, but we provide a mock or a simple input dialog.
        // For now, we'll return null to indicate that the UI should rely on HID input.
        onResult(null)
    }
}
