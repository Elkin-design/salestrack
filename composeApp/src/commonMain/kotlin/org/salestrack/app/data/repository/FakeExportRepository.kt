package org.salestrack.app.data.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportArtifact
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.model.ExportReportPayload
import org.salestrack.app.domain.repository.ExportRepository

class FakeExportRepository : ExportRepository {
    override suspend fun exportReport(
        payload: ExportReportPayload,
        format: ExportFormat,
        destination: ExportDestination,
    ): AppResult<ExportArtifact> {
        val extension = when (format) {
            ExportFormat.Pdf -> "pdf"
            ExportFormat.Excel -> "xlsx"
            ExportFormat.Csv -> "csv"
        }
        val mimeType = when (format) {
            ExportFormat.Pdf -> "application/pdf"
            ExportFormat.Excel -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ExportFormat.Csv -> "text/csv"
        }
        val preview = "${payload.title} | filas=${payload.rows.size} | total=${payload.totalAmount}"

        return AppResult.Success(
            ExportArtifact(
                fileName = "salestrack_report.$extension",
                mimeType = mimeType,
                destination = destination,
                preview = preview,
            ),
        )
    }
}
