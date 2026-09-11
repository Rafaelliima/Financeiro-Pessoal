package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.ReminderItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {

    fun scheduleReminder(context: Context, reminder: ReminderItem) {
        if (reminder.isPaid) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val dateObj = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(reminder.date)
        } catch (_: Exception) { null } ?: return

        val cal = Calendar.getInstance().apply {
            time = dateObj
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()

        // 1. Notificação no dia do vencimento (às 09:00)
        if (reminder.notifyOnDueDate && cal.timeInMillis > now) {
            val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("reminder_name", reminder.name)
                putExtra("reminder_value", reminder.value)
                putExtra("is_day_before", false)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
            } catch (_: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        }

        // 2. Notificação 1 dia antes (às 09:00)
        if (reminder.notifyOneDayBefore) {
            val dayBeforeCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            if (dayBeforeCal.timeInMillis > now) {
                val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
                    putExtra("reminder_id", reminder.id)
                    putExtra("reminder_name", reminder.name)
                    putExtra("reminder_value", reminder.value)
                    putExtra("is_day_before", true)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminder.id.hashCode() + 100000,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dayBeforeCal.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, dayBeforeCal.timeInMillis, pendingIntent)
                    }
                } catch (_: SecurityException) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, dayBeforeCal.timeInMillis, pendingIntent)
                }
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent1 = Intent(context, ReminderNotificationReceiver::class.java)
        val p1 = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent1,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (p1 != null) {
            alarmManager.cancel(p1)
            p1.cancel()
        }

        val intent2 = Intent(context, ReminderNotificationReceiver::class.java)
        val p2 = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 100000,
            intent2,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (p2 != null) {
            alarmManager.cancel(p2)
            p2.cancel()
        }
    }
}
