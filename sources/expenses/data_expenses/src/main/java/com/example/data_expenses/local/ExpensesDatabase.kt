package com.example.data_expenses.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class ExpensesDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var instance: ExpensesDatabase? = null

        fun getInstance(context: Context): ExpensesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExpensesDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
        }

        private const val DB_NAME = "expenses.db"
    }
}
