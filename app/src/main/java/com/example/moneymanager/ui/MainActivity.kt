package com.example.moneymanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.feature_expenses.mvi.ExpensesContainer
import com.example.feature_expenses.ui.ExpensesRoute
import com.example.moneymanager.ui.theme.MoneyManagerTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val expensesContainer: ExpensesContainer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoneyManagerTheme {
                ExpensesRoute(container = expensesContainer)
            }
        }
    }
}