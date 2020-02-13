package com.hiteshsahu.notificationlistener.notification

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CustomNotificationListenerService : NotificationListenerService() {

    private var commandFromUIReceiver: CommandFromUIReceiver? = null


    override fun onCreate() {
        super.onCreate()

        // Register broadcast from UI
        commandFromUIReceiver = CommandFromUIReceiver()
        val filter = IntentFilter()
        filter.addAction(READ_COMMAND_ACTION)
        registerReceiver(commandFromUIReceiver, filter)
    }


    /**
     * New Notn Added Callback
     */
    override fun onNotificationPosted(newNotification: StatusBarNotification) {
        Log.i(
            TAG,
            "-------- onNotificationPosted(): " + "ID :" + newNotification.id + "\t" + newNotification.notification.tickerText + "\t" + newNotification.packageName
        )
        sendResultOnUI("onNotificationPosted :" + newNotification.packageName + "\n")
    }

    /**
     * Notn Removed callback
     */
    override fun onNotificationRemoved(removedNotification: StatusBarNotification) {
        Log.i(
            TAG,
            "-------- onNotificationRemoved() :" + "ID :" + removedNotification.id + "\t" + removedNotification.notification.tickerText + "\t" + removedNotification.packageName
        )
        sendResultOnUI("onNotificationRemoved: " + removedNotification.packageName + "\n")
    }


    internal inner class CommandFromUIReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(COMMAND_KEY) == CLEAR_NOTIFICATIONS)
                 // remove Notns
                cancelAllNotifications()
            else if (intent.getStringExtra(COMMAND_KEY) == GET_ACTIVE_NOTIFICATIONS)
                // Read Notns
                fetchCurrentNotifications()
        }
    }


    /**
     * Fetch list of Active Notns
     */
    private fun fetchCurrentNotifications() {
        sendResultOnUI("===== Notification List START ====")

        val activeNotnCount = this@CustomNotificationListenerService.activeNotifications.size

        if (activeNotnCount > 0) {
            for (count in 0..activeNotnCount) {
                val sbn = this@CustomNotificationListenerService.activeNotifications[count]
                sendResultOnUI("#" + count.toString() + " Package: " + sbn.packageName + "\n")
            }
        } else {
            sendResultOnUI("No active Notn found")
        }

        sendResultOnUI("===== Notification List END====")
    }


    // sendMessage success result on UI
    private fun sendResultOnUI(result: String?) {
        val resultIntent = Intent(UPDATE_UI_ACTION)
        resultIntent.putExtra(RESULT_KEY, Activity.RESULT_OK)
        resultIntent.putExtra(RESULT_VALUE, result)
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(commandFromUIReceiver)
    }

    companion object {


        const val TAG = "NotificationListener"

        //Update UI action
        const val UPDATE_UI_ACTION =   "ACTION_UPDATE_UI"
        const val READ_COMMAND_ACTION = "ACTION_READ_COMMAND"


        // Bundle Key Value Pair
        const val RESULT_KEY = "readResultKey"
        const val RESULT_VALUE = "readResultValue"


        //Actions sent from UI
        const val COMMAND_KEY = "READ_COMMAND"
        const val CLEAR_NOTIFICATIONS = "clearall"
        const val GET_ACTIVE_NOTIFICATIONS = "list"


    }
}