package app.android.adreal.vault.onesignal

import android.util.Log
import com.onesignal.notifications.IDisplayableMutableNotification
import com.onesignal.notifications.INotificationReceivedEvent
import com.onesignal.notifications.INotificationServiceExtension

class NotificationServiceExtension : INotificationServiceExtension {
    override fun onNotificationReceived(event: INotificationReceivedEvent) {
        Log.d("NotificationServiceExtension", "onNotificationReceived: ${event.notification}")
        val notification: IDisplayableMutableNotification = event.notification
        event.preventDefault()
    }
}