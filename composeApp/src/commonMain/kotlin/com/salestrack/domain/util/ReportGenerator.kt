package com.salestrack.domain.util

import com.salestrack.domain.model.ReportData
import com.salestrack.domain.model.Sale

interface ReportGenerator {
    /**
     * Generates a PDF report from the provided data.
     * Returns the generated file as a ByteArray for platform-specific saving/sharing.
     */
    fun generatePdf(title: String, data: ReportData, sales: List<Sale>): ByteArray

    /**
     * Generates an Excel report from the provided sales list.
     * Returns the generated file as a ByteArray.
     */
    fun generateExcel(title: String, sales: List<Sale>): ByteArray
}
