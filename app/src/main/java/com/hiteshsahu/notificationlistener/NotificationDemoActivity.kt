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
package com.hiteshsahu.notificationlistener

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.hiteshsahu.notificationlistener.activity.dummy.BigPictureSocialMainActivity
import com.hiteshsahu.notificationlistener.activity.dummy.BigTextMainActivity
import com.hiteshsahu.notificationlistener.activity.dummy.InboxMainActivity
import com.hiteshsahu.notificationlistener.activity.dummy.MessagingMainActivity
import com.hiteshsahu.notificationlistener.service.BigPictureSocialIntentService
import com.hiteshsahu.notificationlistener.service.BigTextIntentService
import com.hiteshsahu.notificationlistener.service.MessagingIntentService
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.CLEAR_NOTIFICATIONS
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.COMMAND_KEY
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.GET_ACTIVE_NOTIFICATIONS
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.READ_COMMAND_ACTION
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.RESULT_KEY
import com.hiteshsahu.notificationlistener.notification.GlobalNotificationBuilder
import com.hiteshsahu.notificationlistener.notification.MockDatabase
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.RESULT_VALUE
import com.hiteshsahu.notificationlistener.notification.CustomNotificationListenerService.Companion.UPDATE_UI_ACTION
import com.hiteshsahu.notificationlistener.notification.NotificationUtil
import kotlinx.android.synthetic.main.activity_main.*


/**
 * The Activity demonstrates several popular Notification.Style examples along with their best
 * practices.
 *
 * This Activity also demonstrate how to create a custom Notification Listener
 */
class NotificationDemoActivity : AppCompatActivity(), OnItemSelectedListener {
    private var mNotificationManagerCompat: NotificationManagerCompat? = null
    private var mSelectedNotification = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNotificationManagerCompat = NotificationManagerCompat.from(applicationContext)


        //Launch Notn
        btnCreateNotn.setOnClickListener {
            createNewNotification()
        }

        // Remove Notn
        btnClearNotn.setOnClickListener {
            clearNotifications()
        }

        // Read Notns
        btnFeatchNotn.setOnClickListener {
            readNotifications()
        }

        // set up Notn Types
        setUpSpinner()
        //service is enabled do something
    }



    // --------------------------------------------------Notification Listener Workflow--------------------------------------------

    override fun onResume() {
        super.onResume()

        //Register to Broadcast for Updating UI
        LocalBroadcastManager.getInstance(this).registerReceiver(
            clientLooperReceiver,
            IntentFilter(UPDATE_UI_ACTION)
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        //UnRegister to Broadcast for Updating UI
        LocalBroadcastManager.getInstance(this).unregisterReceiver(clientLooperReceiver)
    }

    private fun readNotifications() {
        val i = Intent(READ_COMMAND_ACTION)
        i.putExtra(COMMAND_KEY, GET_ACTIVE_NOTIFICATIONS)
        sendBroadcast(i)
    }

    private fun clearNotifications() {
        val i = Intent(READ_COMMAND_ACTION)
        i.putExtra(COMMAND_KEY, CLEAR_NOTIFICATIONS)
        sendBroadcast(i)
    }

    // Define the callback for what to do when data is received
    private val clientLooperReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val resultCode = intent.getIntExtra(RESULT_KEY, RESULT_CANCELED)
            if (resultCode == RESULT_OK) {

                val resultValue = intent.getStringExtra(RESULT_VALUE)

                clientInput.text =
                    (clientInput.text.toString()
                            + "\n"
                            + resultValue)

                clientScrollView.post {
                    clientScrollView.fullScroll(View.FOCUS_DOWN)
                }

            }
        }
    }

    // --------------------------------------------------_Create  NewNotn WorkFlow--------------------------------------------


    /*+
      Add a new Notn based on chosen Style
    */
    private fun createNewNotification() {
        Log.d(TAG, "onSubmit()")
        val areNotificationsEnabled =
            mNotificationManagerCompat!!.areNotificationsEnabled()

        if (!areNotificationsEnabled) {
            // Because the user took an action to create a notification, we create a prompt to let
            // the user re-enable notifications for this application again.
            val snackbar = Snackbar
                .make(
                    window.decorView.rootView,
                    "You need to enable notifications for this app",
                    Snackbar.LENGTH_LONG
                )
                .setAction("ENABLE") {
                    // Links to this app's notification settings
                    openNotificationSettingsForApp()
                }
            snackbar.show()
            return
        }


        val notificationStyle =
            NOTIFICATION_STYLES[mSelectedNotification]
        when (notificationStyle) {
            BIG_TEXT_STYLE -> generateBigTextStyleNotification()
            BIG_PICTURE_STYLE -> generateBigPictureStyleNotification()
            INBOX_STYLE -> generateInboxStyleNotification()
            MESSAGING_STYLE -> generateMessagingStyleNotification()
            else -> {
            }
        }
    }



    // Create an ArrayAdapter using the string array and a default spinner layout.
    private fun setUpSpinner() {
        val adapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            NOTIFICATION_STYLES
        )
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner.
        spinner!!.adapter = adapter
        spinner!!.onItemSelectedListener = this
    }


    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) {
        Log.d(
            TAG,
            "onItemSelected(): position: $position id: $id"
        )
        mSelectedNotification = position
        notificationDetails!!.text = NOTIFICATION_STYLES_DESCRIPTION[mSelectedNotification]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Required
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that supports both phone/tablet and wear. For devices
     * on API level 16 (4.1.x - Jelly Bean) and after, displays BIG_TEXT_STYLE. Otherwise, displays
     * a basic notification.
     */
    private fun generateBigTextStyleNotification() {
        Log.d(TAG, "generateBigTextStyleNotification()")
        // Main steps for building a BIG_TEXT_STYLE notification:
//      0. Get your data
//      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
//      2. Build the BIG_TEXT_STYLE
//      3. Set up main Intent for notification
//      4. Create additional Actions for the Notification
//      5. Build and issue the notification
// 0. Get your data (everything unique per Notification).
        val bigTextStyleReminderAppData =
            MockDatabase.bigTextStyleData
        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        val notificationChannelId =
            bigTextStyleReminderAppData?.let {
                NotificationUtil.createNotificationChannel(this,
                    it
                )
            }
        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle =
            NotificationCompat.BigTextStyle() // Overrides ContentText in the big form of the template.
                .bigText(bigTextStyleReminderAppData!!.bigText) // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(bigTextStyleReminderAppData.bigContentTitle) // Summary line after the detail section in the big form of the template.
// Note: To improve readability, don't overload the user with info. If Summary Text
// doesn't add critical information, you should skip it.
                .setSummaryText(bigTextStyleReminderAppData.summaryText)
        // 3. Set up main Intent for notification.
        val notifyIntent = Intent(this, BigTextMainActivity::class.java)
        // When creating your Intent, you need to take into account the back state, i.e., what
// happens after your Activity launches and the user presses the back button.
// There are two options:
//      1. Regular activity - You're starting an Activity that's part of the application's
//      normal workflow.
//      2. Special activity - The user only sees this Activity if it's started from a
//      notification. In a sense, the Activity extends the notification by providing
//      information that would be hard to display in the notification itself.
// For the BIG_TEXT_STYLE notification, we will consider the activity launched by the main
// Intent as a special activity, so we will follow option 2.
// For an example of option 1, check either the MESSAGING_STYLE or BIG_PICTURE_STYLE
// examples.
// For more information, check out our dev article:
// https://developer.android.com/training/notify-user/navigation.html
// Sets the Activity to start in a new, empty task
        notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notifyPendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 4. Create additional Actions (Intents) for the Notification.
// In our case, we create two additional actions: a Snooze action and a Dismiss action.
// Snooze Action.
        val snoozeIntent = Intent(this, BigTextIntentService::class.java)
        snoozeIntent.action = BigTextIntentService.ACTION_SNOOZE
        val snoozePendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0)
        val snoozeAction =
            NotificationCompat.Action.Builder(
                R.drawable.ic_alarm_white_48dp,
                "Snooze",
                snoozePendingIntent
            )
                .build()
        // Dismiss Action.
        val dismissIntent = Intent(this, BigTextIntentService::class.java)
        dismissIntent.action = BigTextIntentService.ACTION_DISMISS
        val dismissPendingIntent =
            PendingIntent.getService(this, 0, dismissIntent, 0)
        val dismissAction =
            NotificationCompat.Action.Builder(
                R.drawable.ic_cancel_white_48dp,
                "Dismiss",
                dismissPendingIntent
            )
                .build()
        // 5. Build and issue the notification.
// Because we want this to be a new notification (not updating a previous notification), we
// create a new Builder. Later, we use the same global builder to get back the notification
// we built here for the snooze action, that is, canceling the notification and relaunching
// it several seconds later.
// Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder = NotificationCompat.Builder(
            applicationContext, notificationChannelId!!
        )
        GlobalNotificationBuilder.notificationCompatBuilderInstance = notificationCompatBuilder
        val notification =
            notificationCompatBuilder // BIG_TEXT_STYLE sets title and content for API 16 (4.1 and after).
                .setStyle(bigTextStyle) // Title for API <16 (4.0 and below) devices.
                .setContentTitle(bigTextStyleReminderAppData.contentTitle) // Content for API <24 (7.0 and below) devices.
                .setContentText(bigTextStyleReminderAppData.contentText)
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_alarm_white_48dp
                    )
                )
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Set primary color (important for Wear 2.0 Notifications).
                .setColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorPrimary
                    )
                ) // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
// devices and all Wear devices. If you have more than one notification and
// you prefer a different summary notification, set a group key and create a
// summary notification via
// .setGroupSummary(true)
// .setGroup(GROUP_KEY_YOUR_NAME_HERE)
                .setCategory(Notification.CATEGORY_REMINDER) // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
// 'importance' which is set in the NotificationChannel. The integers representing
// 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(bigTextStyleReminderAppData.priority) // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
// visibility is set in the NotificationChannel.
                .setVisibility(bigTextStyleReminderAppData!!.channelLockscreenVisibility) // Adds additional actions specified above.
                .addAction(snoozeAction)
                .addAction(dismissAction)
                .build()
        mNotificationManagerCompat!!.notify(NOTIFICATION_ID, notification)
    }

    /*
     * Generates a BIG_PICTURE_STYLE Notification that supports both phone/tablet and wear. For
     * devices on API level 16 (4.1.x - Jelly Bean) and after, displays BIG_PICTURE_STYLE.
     * Otherwise, displays a basic notification.
     *
     * This example Notification is a social post. It allows updating the notification with
     * comments/responses via RemoteInput and the BigPictureSocialIntentService on 24+ (N+) and
     * Wear devices.
     */
    private fun generateBigPictureStyleNotification() {
        Log.d(TAG, "generateBigPictureStyleNotification()")
        // Main steps for building a BIG_PICTURE_STYLE notification:
//      0. Get your data
//      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
//      2. Build the BIG_PICTURE_STYLE
//      3. Set up main Intent for notification
//      4. Set up RemoteInput, so users can input (keyboard and voice) from notification
//      5. Build and issue the notification
// 0. Get your data (everything unique per Notification).
        val bigPictureStyleSocialAppData =
            MockDatabase.bigPictureStyleData
        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        val notificationChannelId =
            bigPictureStyleSocialAppData?.let {
                NotificationUtil.createNotificationChannel(this,
                    it
                )
            }
        // 2. Build the BIG_PICTURE_STYLE.
        val bigPictureStyle =
            NotificationCompat.BigPictureStyle() // Provides the bitmap for the BigPicture notification.
                .bigPicture(
                    BitmapFactory.decodeResource(
                        resources,
                        bigPictureStyleSocialAppData!!.bigImage
                    )
                ) // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(bigPictureStyleSocialAppData.bigContentTitle) // Summary line after the detail section in the big form of the template.
                .setSummaryText(bigPictureStyleSocialAppData.summaryText)
        // 3. Set up main Intent for notification.
        val mainIntent = Intent(this, BigPictureSocialMainActivity::class.java)
        // When creating your Intent, you need to take into account the back state, i.e., what
// happens after your Activity launches and the user presses the back button.
// There are two options:
//      1. Regular activity - You're starting an Activity that's part of the application's
//      normal workflow.
//      2. Special activity - The user only sees this Activity if it's started from a
//      notification. In a sense, the Activity extends the notification by providing
//      information that would be hard to display in the notification itself.
// Even though this sample's MainActivity doesn't link to the Activity this Notification
// launches directly, i.e., it isn't part of the normal workflow, a social app generally
// always links to individual posts as part of the app flow, so we will follow option 1.
// For an example of option 2, check out the BIG_TEXT_STYLE example.
// For more information, check out our dev article:
// https://developer.android.com/training/notify-user/navigation.html
        val stackBuilder =
            TaskStackBuilder.create(this)
        // Adds the back stack.
        stackBuilder.addParentStack(BigPictureSocialMainActivity::class.java)
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent)
        // Gets a PendingIntent containing the entire back stack.
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.
// Note: For API <24 (M and below) we need to use an Activity, so the lock-screen presents
// the auth challenge. For API 24+ (N and above), we use a Service (could be a
// BroadcastReceiver), so the user can input from Notification or lock-screen (they have
// choice to allow) without leaving the notification.
// Create the RemoteInput.
        val replyLabel = getString(R.string.reply_label)
        val remoteInput =
            RemoteInput.Builder(BigPictureSocialIntentService.EXTRA_COMMENT)
                .setLabel(replyLabel) // List of quick response choices for any wearables paired with the phone
                .setChoices(bigPictureStyleSocialAppData.possiblePostResponses)
                .build()
        // Pending intent =
//      API <24 (M and below): activity so the lock-screen presents the auth challenge
//      API 24+ (N and above): this should be a Service or BroadcastReceiver
        val replyActionPendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(this, BigPictureSocialIntentService::class.java)
            intent.action = BigPictureSocialIntentService.ACTION_COMMENT
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
        // 5. Build and issue the notification.
// Because we want this to be a new notification (not updating a previous notification), we
// create a new Builder. Later, we use the same global builder to get back the notification
// we built here for a comment on the post.
        val notificationCompatBuilder =
            notificationChannelId?.let { NotificationCompat.Builder(applicationContext, it) }
        GlobalNotificationBuilder.notificationCompatBuilderInstance = notificationCompatBuilder
        notificationCompatBuilder!! // BIG_PICTURE_STYLE sets title and content for API 16 (4.1 and after).
            .setStyle(bigPictureStyle) // Title for API <16 (4.0 and below) devices.
            .setContentTitle(bigPictureStyleSocialAppData.contentTitle) // Content for API <24 (7.0 and below) devices.
            .setContentText(bigPictureStyleSocialAppData.contentText)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_person_black_48dp
                )
            )
            .setContentIntent(mainPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Set primary color (important for Wear 2.0 Notifications).
            .setColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimary
                )
            ) // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
// devices and all Wear devices. If you have more than one notification and
// you prefer a different summary notification, set a group key and create a
// summary notification via
// .setGroupSummary(true)
// .setGroup(GROUP_KEY_YOUR_NAME_HERE)
            .setSubText(Integer.toString(1))
            .addAction(replyAction)
            .setCategory(Notification.CATEGORY_SOCIAL) // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
// 'importance' which is set in the NotificationChannel. The integers representing
// 'priority' are different from 'importance', so make sure you don't mix them.
            .setPriority(bigPictureStyleSocialAppData.priority) // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
// visibility is set in the NotificationChannel.
            .setVisibility(bigPictureStyleSocialAppData.channelLockscreenVisibility)
        // If the phone is in "Do not disturb mode, the user will still be notified if
// the sender(s) is starred as a favorite.
        for (name in bigPictureStyleSocialAppData.participants) {
            notificationCompatBuilder.addPerson(name)
        }
        val notification = notificationCompatBuilder.build()
        mNotificationManagerCompat!!.notify(NOTIFICATION_ID, notification)
    }

    /*
     * Generates a INBOX_STYLE Notification that supports both phone/tablet and wear. For devices
     * on API level 16 (4.1.x - Jelly Bean) and after, displays INBOX_STYLE. Otherwise, displays a
     * basic notification.
     */
    private fun generateInboxStyleNotification() {
        Log.d(TAG, "generateInboxStyleNotification()")
        // Main steps for building a INBOX_STYLE notification:
//      0. Get your data
//      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
//      2. Build the INBOX_STYLE
//      3. Set up main Intent for notification
//      4. Build and issue the notification
// 0. Get your data (everything unique per Notification).
        val inboxStyleEmailAppData = MockDatabase.inboxStyleData
        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        val notificationChannelId =
            inboxStyleEmailAppData?.let { NotificationUtil.createNotificationChannel(this, it) }
        // 2. Build the INBOX_STYLE.
        val inboxStyle =
            NotificationCompat.InboxStyle() // This title is slightly different than regular title, since I know INBOX_STYLE is
// available.
                .setBigContentTitle(inboxStyleEmailAppData!!.bigContentTitle)
                .setSummaryText(inboxStyleEmailAppData!!.summaryText)
        // Add each summary line of the new emails, you can add up to 5.
        for (summary in inboxStyleEmailAppData!!.individualEmailSummary) {
            inboxStyle.addLine(summary)
        }
        // 3. Set up main Intent for notification.
        val mainIntent = Intent(this, InboxMainActivity::class.java)
        // When creating your Intent, you need to take into account the back state, i.e., what
// happens after your Activity launches and the user presses the back button.
// There are two options:
//      1. Regular activity - You're starting an Activity that's part of the application's
//      normal workflow.
//      2. Special activity - The user only sees this Activity if it's started from a
//      notification. In a sense, the Activity extends the notification by providing
//      information that would be hard to display in the notification itself.
// Even though this sample's MainActivity doesn't link to the Activity this Notification
// launches directly, i.e., it isn't part of the normal workflow, a eamil app generally
// always links to individual emails as part of the app flow, so we will follow option 1.
// For an example of option 2, check out the BIG_TEXT_STYLE example.
// For more information, check out our dev article:
// https://developer.android.com/training/notify-user/navigation.html
        val stackBuilder =
            TaskStackBuilder.create(this)
        // Adds the back stack.
        stackBuilder.addParentStack(InboxMainActivity::class.java)
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent)
        // Gets a PendingIntent containing the entire back stack.
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 4. Build and issue the notification.
// Because we want this to be a new notification (not updating a previous notification), we
// create a new Builder. However, we don't need to update this notification later, so we
// will not need to set a global builder for access to the notification later.
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, notificationChannelId!!)
        GlobalNotificationBuilder.notificationCompatBuilderInstance = notificationCompatBuilder
        notificationCompatBuilder // INBOX_STYLE sets title and content for API 16+ (4.1 and after) when the
// notification is expanded.
            .setStyle(inboxStyle) // Title for API <16 (4.0 and below) devices and API 16+ (4.1 and after) when the
// notification is collapsed.
            .setContentTitle(inboxStyleEmailAppData!!.contentTitle) // Content for API <24 (7.0 and below) devices and API 16+ (4.1 and after) when the
// notification is collapsed.
            .setContentText(inboxStyleEmailAppData!!.contentText)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_person_black_48dp
                )
            )
            .setContentIntent(mainPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Set primary color (important for Wear 2.0 Notifications).
            .setColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimary
                )
            ) // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
// devices and all Wear devices. If you have more than one notification and
// you prefer a different summary notification, set a group key and create a
// summary notification via
// .setGroupSummary(true)
// .setGroup(GROUP_KEY_YOUR_NAME_HERE)
// Sets large number at the right-hand side of the notification for API <24 devices.
            .setSubText(Integer.toString(inboxStyleEmailAppData.numberOfNewEmails))
            .setCategory(Notification.CATEGORY_EMAIL) // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
// 'importance' which is set in the NotificationChannel. The integers representing
// 'priority' are different from 'importance', so make sure you don't mix them.
            .setPriority(inboxStyleEmailAppData.priority) // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
// visibility is set in the NotificationChannel.
            .setVisibility(inboxStyleEmailAppData.channelLockscreenVisibility)
        // If the phone is in "Do not disturb mode, the user will still be notified if
// the sender(s) is starred as a favorite.
        for (name in inboxStyleEmailAppData.participants) {
            notificationCompatBuilder.addPerson(name)
        }
        val notification = notificationCompatBuilder.build()
        mNotificationManagerCompat!!.notify(NOTIFICATION_ID, notification)
    }

    /*
     * Generates a MESSAGING_STYLE Notification that supports both phone/tablet and wear. For
     * devices on API level 24 (7.0 - Nougat) and after, displays MESSAGING_STYLE. Otherwise,
     * displays a basic BIG_TEXT_STYLE.
     */
    private fun generateMessagingStyleNotification() {
        Log.d(TAG, "generateMessagingStyleNotification()")
        // Main steps for building a MESSAGING_STYLE notification:
//      0. Get your data
//      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
//      2. Build the MESSAGING_STYLE
//      3. Set up main Intent for notification
//      4. Set up RemoteInput (users can input directly from notification)
//      5. Build and issue the notification
// 0. Get your data (everything unique per Notification)
        val messagingStyleCommsAppData =
            MockDatabase.getMessagingStyleData(applicationContext)
        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        val notificationChannelId =
            NotificationUtil.createNotificationChannel(this, messagingStyleCommsAppData!!)
        // 2. Build the NotificationCompat.Style (MESSAGING_STYLE).
        val contentTitle = messagingStyleCommsAppData!!.contentTitle
        val messagingStyle =
            NotificationCompat.MessagingStyle(messagingStyleCommsAppData.me) /*
                         * <p>This API's behavior was changed in SDK version
                         * {@link Build.VERSION_CODES#P}. If your application's target version is
                         * less than {@link Build.VERSION_CODES#P}, setting a conversation title to
                         * a non-null value will make {@link #isGroupConversation()} return
                         * {@code true} and passing {@code null} will make it return {@code false}.
                         * This behavior can be overridden by calling
                         * {@link #setGroupConversation(boolean)} regardless of SDK version.
                         * In {@code P} and above, this method does not affect group conversation
                         * settings.
                         *
                         * In our case, we use the same title.
                         */
                .setConversationTitle(contentTitle)
        // Adds all Messages.
// Note: Messages include the text, timestamp, and sender.
        for (message in messagingStyleCommsAppData.messages) {
            messagingStyle.addMessage(message)
        }
        messagingStyle.isGroupConversation = messagingStyleCommsAppData.isGroupConversation
        // 3. Set up main Intent for notification.
        val notifyIntent = Intent(this, MessagingMainActivity::class.java)
        // When creating your Intent, you need to take into account the back state, i.e., what
// happens after your Activity launches and the user presses the back button.
// There are two options:
//      1. Regular activity - You're starting an Activity that's part of the application's
//      normal workflow.
//      2. Special activity - The user only sees this Activity if it's started from a
//      notification. In a sense, the Activity extends the notification by providing
//      information that would be hard to display in the notification itself.
// Even though this sample's MainActivity doesn't link to the Activity this Notification
// launches directly, i.e., it isn't part of the normal workflow, a chat app generally
// always links to individual conversations as part of the app flow, so we will follow
// option 1.
// For an example of option 2, check out the BIG_TEXT_STYLE example.
// For more information, check out our dev article:
// https://developer.android.com/training/notify-user/navigation.html
        val stackBuilder =
            TaskStackBuilder.create(this)
        // Adds the back stack
        stackBuilder.addParentStack(MessagingMainActivity::class.java)
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(notifyIntent)
        // Gets a PendingIntent containing the entire back stack
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.
// Note: For API <24 (M and below) we need to use an Activity, so the lock-screen present
// the auth challenge. For API 24+ (N and above), we use a Service (could be a
// BroadcastReceiver), so the user can input from Notification or lock-screen (they have
// choice to allow) without leaving the notification.
// Create the RemoteInput specifying this key.
        val replyLabel = getString(R.string.reply_label)
        val remoteInput =
            RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
                .setLabel(replyLabel) // Use machine learning to create responses based on previous messages.
                .setChoices(messagingStyleCommsAppData.replyChoicesBasedOnLastMessage)
                .build()
        // Pending intent =
//      API <24 (M and below): activity so the lock-screen presents the auth challenge.
//      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        val replyActionPendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(this, MessagingIntentService::class.java)
            intent.action = MessagingIntentService.ACTION_REPLY
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
                .addRemoteInput(remoteInput) // Informs system we aren't bringing up our own custom UI for a reply
// action.
                .setShowsUserInterface(false) // Allows system to generate replies by context of conversation.
                .setAllowGeneratedReplies(true)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .build()
        // 5. Build and issue the notification.
// Because we want this to be a new notification (not updating current notification), we
// create a new Builder. Later, we update this same notification, so we need to save this
// Builder globally (as outlined earlier).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, notificationChannelId!!)
        GlobalNotificationBuilder.notificationCompatBuilderInstance = notificationCompatBuilder
        notificationCompatBuilder // MESSAGING_STYLE sets title and content for API 16 and above devices.
            .setStyle(messagingStyle) // Title for API < 16 devices.
            .setContentTitle(contentTitle) // Content for API < 16 devices.
            .setContentText(messagingStyleCommsAppData.contentText)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_person_black_48dp
                )
            )
            .setContentIntent(mainPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Set primary color (important for Wear 2.0 Notifications).
            .setColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimary
                )
            )
            // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
            // devices and all Wear devices. If you have more than one notification and
            // you prefer a different summary notification, set a group key and create a
            // summary notification via
            // .setGroupSummary(true)
            // .setGroup(GROUP_KEY_YOUR_NAME_HERE)
            // Number of new notifications for API <24 (M and below) devices.
            .setSubText(Integer.toString(messagingStyleCommsAppData.numberOfNewMessages))
            .addAction(replyAction)
            .setCategory(Notification.CATEGORY_MESSAGE) // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
            // 'importance' which is set in the NotificationChannel. The integers representing
            // 'priority' are different from 'importance', so make sure you don't mix them.
            .setPriority(messagingStyleCommsAppData.priority) // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
            // visibility is set in the NotificationChannel.
            .setVisibility(messagingStyleCommsAppData.channelLockscreenVisibility)
                 // If the phone is in "Do not disturb" mode, the user may still be notified if the
            // sender(s) are in a group allowed through "Do not disturb" by the user.
        for (name in messagingStyleCommsAppData.participants) {
            notificationCompatBuilder.addPerson(name.uri)
        }
        val notification = notificationCompatBuilder.build()
        mNotificationManagerCompat!!.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Helper method for the SnackBar action, i.e., if the user has this application's notifications
     * disabled, this opens up the dialog to turn them back on after the user requests a
     * Notification launch.
     *
     * IMPORTANT NOTE: You should not do this action unless the user takes an action to see your
     * Notifications like this sample demonstrates. Spamming users to re-enable your notifications
     * is a bad idea.
     */
    private fun openNotificationSettingsForApp() { // Links to this app's notification settings.
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        startActivity(intent)
    }


    companion object {
        const val TAG = "MainActivity"
        const val NOTIFICATION_ID = 888
        // Used for Notification Style array and switch statement for Spinner selection.
        private const val BIG_TEXT_STYLE = "BIG_TEXT_STYLE"
        private const val BIG_PICTURE_STYLE = "BIG_PICTURE_STYLE"
        private const val INBOX_STYLE = "INBOX_STYLE"
        private const val MESSAGING_STYLE = "MESSAGING_STYLE"
        // Collection of notification styles to back ArrayAdapter for Spinner.
        private val NOTIFICATION_STYLES = arrayOf<String?>(
            BIG_TEXT_STYLE,
            BIG_PICTURE_STYLE,
            INBOX_STYLE,
            MESSAGING_STYLE
        )
        private val NOTIFICATION_STYLES_DESCRIPTION =
            arrayOf(
                "Demos reminder type app using BIG_TEXT_STYLE",
                "Demos social type app using BIG_PICTURE_STYLE + inline notification response",
                "Demos email type app using INBOX_STYLE",
                "Demos messaging app using MESSAGING_STYLE + inline notification responses"
            )
    }


}