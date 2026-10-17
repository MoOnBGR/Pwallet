package com.example.pwallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.pwallet.data.local.entity.AuditLogEntity
import com.example.pwallet.data.local.entity.ClientEntity
import com.example.pwallet.data.local.entity.InstallmentEntity
import com.example.pwallet.data.local.entity.LoanEntity

@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: AuditLogEntity)
}

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(client: ClientEntity)
}

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(loan: LoanEntity)
}

@Dao
interface InstallmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<InstallmentEntity>)
}
