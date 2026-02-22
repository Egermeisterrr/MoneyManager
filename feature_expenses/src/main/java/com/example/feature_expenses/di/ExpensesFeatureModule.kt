package com.example.feature_expenses.di

import com.example.domain_expenses.AddExpenseUseCase
import com.example.domain_expenses.ExpensesRepository
import com.example.domain_expenses.FilterExpensesByPeriodUseCase
import com.example.domain_expenses.GetExpensesUseCase
import com.example.feature_expenses.mvi.ExpensesContainer
import com.example.feature_expenses.repository.SharedPreferencesExpensesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val expensesFeatureModule = module {
    single<ExpensesRepository> { SharedPreferencesExpensesRepository(androidContext()) }
    factory { GetExpensesUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { FilterExpensesByPeriodUseCase() }
    single { ExpensesContainer(get(), get(), get()) }
}
