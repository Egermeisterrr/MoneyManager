package com.example.moneymanager.di

import android.app.Application
import com.example.feature_expenses.di.expensesFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MoneyManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoneyManagerApp)
            modules(expensesFeatureModule)
        }
    }
}
