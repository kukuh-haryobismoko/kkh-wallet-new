package com.kkh.wallet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.kkh.wallet.domain.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class KKHWalletApplication : Application() {

    @Inject lateinit var categoryRepository: CategoryRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        appScope.launch { categoryRepository.seedDefaultsIfEmpty() }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BILLS,
                "Bills & Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Credit card & paylater due-date reminders" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Spending and budget warnings" }
        )
    }

    companion object {
        const val CHANNEL_BILLS = "channel_bills"
        const val CHANNEL_BUDGET = "channel_budget"
    }
}
