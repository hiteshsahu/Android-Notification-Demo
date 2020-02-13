/*
Copyright 2016 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.hiteshsahu.notificationlistener.service

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import com.hiteshsahu.notificationlistener.NotificationDemoActivity
import com.hiteshsahu.notificationlistener.R
import com.hiteshsahu.notificationlistener.activity.dummy.BigPictureSocialMainActivity
import com.hiteshsahu.notificationlistener.notification.GlobalNotificationBuilder.notificationCompatBuilderInstance
import com.hiteshsahu.notificationlistener.notification.MockDatabase

/**
 * Asynchronously handles updating social app posts (and active Notification) with comments from
 * user. Notification for social app use BigPictureStyle.
 */
class BigPictureSocialIntentService : IntentService("BigPictureSocialIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        Log.d(
            TAG,
            "onHandleIntent(): $intent"
        )
        if (intent != null) {
            val action = intent.action
            if (ACTION_COMMENT == action) {
                handleActionComment(getMessage(intent))
            }
        }
    }

    /**
     * Handles action for adding a comment from the notification.
     */
    private fun handleActionComment(comment: CharSequence?) {
        Log.d(
            TAG,
            "handleActionComment(): $comment"
        )
        if (comment != null) { // TODO: Asynchronously save your message to Database and servers.
/*
             * You have two options for updating your notification (this class uses approach #2):
             *
             *  1. Use a new NotificationCompatBuilder to create the Notification. This approach
             *  requires you to get *ALL* the information that existed in the previous
             *  Notification (and updates) and pass it to the builder. This is the approach used in
             *  the MainActivity.
             *
             *  2. Use the original NotificationCompatBuilder to create the Notification. This
             *  approach requires you to store a reference to the original builder. The benefit is
             *  you only need the new/updated information. In our case, the comment from the user
             *  regarding the post (which we already have here).
             *
             *  IMPORTANT NOTE: You shouldn't save/modify the resulting Notification object using
             *  its member variables and/or legacy APIs. If you want to retain anything from update
             *  to update, retain the Builder as option 2 outlines.
             */
// Retrieves NotificationCompat.Builder used to create initial Notification
            var notificationCompatBuilder =
                notificationCompatBuilderInstance
            // Recreate builder from persistent state if app process is killed
            if (notificationCompatBuilder == null) { // Note: New builder set globally in the method
                notificationCompatBuilder = recreateBuilderWithBigPictureStyle()
            }
            // Updates active Notification
            val updatedNotification =
                notificationCompatBuilder // Adds a line and comment below content in Notification
                    .setRemoteInputHistory(arrayOf(comment))
                    .build()
            // Pushes out the updated Notification
            val notificationManagerCompat =
                NotificationManagerCompat.from(applicationContext)
            notificationManagerCompat.notify(
                NotificationDemoActivity.NOTIFICATION_ID,
                updatedNotification
            )
        }
    }

    /*
     * Extracts CharSequence created from the RemoteInput associated with the Notification.
     */
    private fun getMessage(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(EXTRA_COMMENT)
    }

    /*
     * This recreates the notification from the persistent state in case the app process was killed.
     * It is basically the same code for creating the Notification from MainActivity.
     */
    private fun recreateBuilderWithBigPictureStyle(): NotificationCompat.Builder { // Main steps for building a BIG_PICTURE_STYLE notification (for more detailed comments on
// building this notification, check MainActivity.java):
//      0. Get your data
//      1. Build the BIG_PICTURE_STYLE
//      2. Set up main Intent for notification
//      3. Set up RemoteInput, so users can input (keyboard and voice) from notification
//      4. Build and issue the notification
// 0. Get your data (everything unique per Notification)
        val bigPictureStyleSocialAppData =
            MockDatabase.bigPictureStyleData
        // 1. Build the BIG_PICTURE_STYLE
        val bigPictureStyle =
            NotificationCompat.BigPictureStyle()
                .bigPicture(
                    BitmapFactory.decodeResource(
                        resources,
                        bigPictureStyleSocialAppData!!.bigImage
                    )
                )
                .setBigContentTitle(bigPictureStyleSocialAppData.bigContentTitle)
                .setSummaryText(bigPictureStyleSocialAppData.summaryText)
        // 2. Set up main Intent for notification
        val mainIntent = Intent(this, BigPictureSocialMainActivity::class.java)
        val stackBuilder =
            TaskStackBuilder.create(this)
        stackBuilder.addParentStack(BigPictureSocialMainActivity::class.java)
        stackBuilder.addNextIntent(mainIntent)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 3. Set up RemoteInput, so users can input (keyboard and voice) from notification
        val replyLabel = getString(R.string.reply_label)
        val remoteInput =
            RemoteInput.Builder(EXTRA_COMMENT)
                .setLabel(replyLabel)
                .setChoices(bigPictureStyleSocialAppData.possiblePostResponses)
                .build()
        val replyActionPendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(this, BigPictureSocialIntentService::class.java)
            intent.action = ACTION_COMMENT
            replyActionPendingIntent = PendingIntent.getService(this, 0, intent, 0)
        } else {
            replyActionPendingIntent = mainPendingIntent
        }
        val replyAction =
            NotificationCompat.Action.Builder(
                R.drawable.ic_reply_white_18dp,
                replyLabel,
                replyActionPendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()
        // 4. Build and issue the notification
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext)
        notificationCompatBuilderInstance = notificationCompatBuilder
        notificationCompatBuilder
            .setStyle(bigPictureStyle)
            .setContentTitle(bigPictureStyleSocialAppData.contentTitle)
            .setContentText(bigPictureStyleSocialAppData.contentText)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_person_black_48dp
                )
            )
            .setContentIntent(mainPendingIntent)
            .setColor(resources.getColor(R.color.colorPrimary))
            .setSubText(Integer.toString(1))
            .addAction(replyAction)
            .setCategory(Notification.CATEGORY_SOCIAL)
            .setPriority(Notification.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        // If the phone is in "Do not disturb mode, the user will still be notified if
// the sender(s) is starred as a favorite.
        for (name in bigPictureStyleSocialAppData.participants) {
            notificationCompatBuilder.addPerson(name)
        }
        return notificationCompatBuilder
    }

    companion object {
        private const val TAG = "BigPictureService"
        const val ACTION_COMMENT =
            "com.example.android.wearable.wear.wearnotifications.handlers.action.COMMENT"
        const val EXTRA_COMMENT =
            "com.example.android.wearable.wear.wearnotifications.handlers.extra.COMMENT"
    }
}