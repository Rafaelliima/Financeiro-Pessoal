package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.JsonStorageManager

/**
 * Receptor acionado após o boot do dispositivo para reagendar todos os alarmes
 * de lembretes financeiros futuros que foram cancelados pelo sistema operacional.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                val storageManager = JsonStorageManager(context)
                val loadResult = storageManager.loadData()
                val reminders = loadResult.data?.reminders ?: emptyList()
                for (reminder in reminders) {
                    if (!reminder.isPaid) {
                        ReminderScheduler.scheduleReminder(context, reminder)
                    }
                }
            } catch (_: Exception) {
                // Previne crash durante inicialização do sistema
            }
        }
    }
}
