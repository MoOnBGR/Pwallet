package com.example.pwallet.data.repository


import com.example.pwallet.data.local.dao.AuditDao
import com.example.pwallet.data.local.entity.AuditLogEntity
import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.repository.AuditLogger

class AuditLoggerImpl(
    private val dao: AuditDao,
    private val now: () -> Long = System::currentTimeMillis,
) : AuditLogger {
    override suspend fun log(action: AuditAction, userId: String?, details: String?) {
        dao.insert(AuditLogEntity(timestamp = now(), userId = userId, action = action, details = details))
    }
}