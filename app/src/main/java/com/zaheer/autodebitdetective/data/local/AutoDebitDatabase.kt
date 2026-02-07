package com.zaheer.autodebitdetective.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zaheer.autodebitdetective.data.local.dao.EntitlementDao
import com.zaheer.autodebitdetective.data.local.dao.RecurringDao
import com.zaheer.autodebitdetective.data.local.dao.TransactionDao
import com.zaheer.autodebitdetective.data.local.entity.EntitlementEntity
import com.zaheer.autodebitdetective.data.local.entity.RecurringEntity
import com.zaheer.autodebitdetective.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        RecurringEntity::class,
        EntitlementEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AutoDebitDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringDao(): RecurringDao
    abstract fun entitlementDao(): EntitlementDao

    companion object {
        private const val DATABASE_NAME = "auto_debit_detective.db"

        @Volatile
        private var INSTANCE: AutoDebitDatabase? = null

        fun getInstance(context: Context): AutoDebitDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AutoDebitDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AutoDebitDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
