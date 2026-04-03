package org.salestrack.app.domain.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportReportPayload

interface PrintRepository {
    suspend fun printReport(payload: ExportReportPayload): AppResult<Unit>
}
