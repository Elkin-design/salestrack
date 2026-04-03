package org.salestrack.app.domain.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.BackupArtifact
import org.salestrack.app.domain.model.BackupPayload

interface BackupRepository {
    suspend fun createBackup(payload: BackupPayload): AppResult<BackupArtifact>
}
