package com.example.pwallet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pwallet.data.local.dao.AuditDao
import com.example.pwallet.data.local.dao.ClientDao
import com.example.pwallet.data.local.dao.InstallmentDao
import com.example.pwallet.data.local.dao.LoanDao
import com.example.pwallet.data.local.dao.UserDao
import com.example.pwallet.data.local.entity.AuditLogEntity
import com.example.pwallet.data.local.entity.ClientEntity
import com.example.pwallet.data.local.entity.InstallmentEntity
import com.example.pwallet.data.local.entity.LoanEntity
import com.example.pwallet.data.local.entity.PaymentEntity
import com.example.pwallet.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class, AuditLogEntity::class, ClientEntity::class,
        LoanEntity::class, InstallmentEntity::class, PaymentEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun auditDao(): AuditDao
    abstract fun clientDao(): ClientDao
    abstract fun loanDao(): LoanDao
    abstract fun installmentDao(): InstallmentDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "pwallet.db")
                .addCallback(AuditTriggersCallback)
                .build()
                .also { instance = it }
        }
    }
}

/** Hace audit_log de solo inserción a nivel de motor. */
private object AuditTriggersCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TRIGGER IF NOT EXISTS audit_log_no_update BEFORE UPDATE ON audit_log " +
                    "BEGIN SELECT RAISE(ABORT, 'audit_log es de solo insercion'); END"
        )
        db.execSQL(
            "CREATE TRIGGER IF NOT EXISTS audit_log_no_delete BEFORE DELETE ON audit_log " +
                    "BEGIN SELECT RAISE(ABORT, 'audit_log es de solo insercion'); END"
        )
    }
}