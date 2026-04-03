package org.salestrack.app.data.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportReportPayload
import org.salestrack.app.domain.repository.PrintRepository

class FakePrintRepository : PrintRepository {
    override suspend fun printReport(payload: ExportReportPayload): AppResult<Unit> {
        return if (payload.rows.isEmpty()) {
            AppResult.Failure(IllegalStateException("No hay datos para imprimir"))
        } else {
            AppResult.Success(Unit)
        }
    }
}
