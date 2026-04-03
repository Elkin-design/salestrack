package org.salestrack.app.domain.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportArtifact
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.model.ExportReportPayload

interface ExportRepository {
    suspend fun exportReport(
        payload: ExportReportPayload,
        format: ExportFormat,
        destination: ExportDestination,
    ): AppResult<ExportArtifact>
}
