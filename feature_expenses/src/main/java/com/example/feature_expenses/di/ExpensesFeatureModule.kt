package com.example.feature_expenses.di

import com.example.data_expenses.repository.ExpensesRepositoryImpl
import com.example.domain_expenses.repository.ExpensesRepository
import com.example.domain_expenses.use_case.AddExpenseUseCase
import com.example.domain_expenses.use_case.DeleteExpenseUseCase
import com.example.domain_expenses.use_case.FilterExpensesByPeriodUseCase
import com.example.domain_expenses.use_case.GetExpensesUseCase
import com.example.feature_expenses.mvi.ExpensesContainer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val expensesFeatureModule = module {
    single<ExpensesRepository> { ExpensesRepositoryImpl(androidContext()) }
    factory { GetExpensesUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { FilterExpensesByPeriodUseCase() }
    single { ExpensesContainer(get(), get(), get(), get()) }
}
