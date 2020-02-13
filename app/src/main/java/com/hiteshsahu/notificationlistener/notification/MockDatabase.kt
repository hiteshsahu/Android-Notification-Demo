/*
 * Copyright (C) 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiteshsahu.notificationlistener.notification

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.hiteshsahu.notificationlistener.R
import java.util.*

/** Mock data for each of the Notification Style Demos.  */
object MockDatabase {
    val bigTextStyleData: BigTextStyleReminderAppData?
        get() = BigTextStyleReminderAppData.instance

    val bigPictureStyleData: BigPictureStyleSocialAppData?
        get() = BigPictureStyleSocialAppData.instance

    val inboxStyleData: InboxStyleEmailAppData?
        get() = InboxStyleEmailAppData.instance

    fun getMessagingStyleData(context: Context): MessagingStyleCommsAppData? {
        return MessagingStyleCommsAppData.getInstance(context)
    }

    fun resourceToUri(context: Context, resId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://"
                    + context.resources.getResourcePackageName(resId)
                    + "/"
                    + context.resources.getResourceTypeName(resId)
                    + "/"
                    + context.resources.getResourceEntryName(resId)
        )
    }

    /** Represents data needed for BigTextStyle Notification.  */
    class BigTextStyleReminderAppData private constructor() : MockNotificationData() {
        // Unique data for this Notification.Style:
        val bigContentTitle: String
        val bigText: String
        val summaryText: String

        override fun toString(): String {
            return bigContentTitle + bigText
        }

        companion object {
            private var sInstance: BigTextStyleReminderAppData? = null
            val instance: BigTextStyleReminderAppData?
                get() {
                    if (sInstance == null) {
                        sInstance =
                            sync
                    }
                    return sInstance
                }

            @get:Synchronized
            private val sync: BigTextStyleReminderAppData?
                private get() {
                    if (sInstance == null) {
                        sInstance = BigTextStyleReminderAppData()
                    }
                    return sInstance
                }
        }

        init { // Standard Notification values:
// Title for API <16 (4.0 and below) devices.
            contentTitle = "Don't forget to..."
            // Content for API <24 (4.0 and below) devices.
            contentText = "Feed Dogs and check garage!"
            priority = NotificationCompat.PRIORITY_DEFAULT
            // BigText Style Notification values:
            bigContentTitle = "Don't forget to..."
            bigText = ("... feed the dogs before you leave for work, and check the garage to "
                    + "make sure the door is closed.")
            summaryText = "Dogs and Garage"
            // Notification channel values (for devices targeting 26 and above):
            channelId = "channel_reminder_1"
            // The user-visible name of the channel.
            channelName = "Sample Reminder"
            // The user-visible description of the channel.
            channelDescription = "Sample Reminder Notifications"
            channelImportance = NotificationManager.IMPORTANCE_DEFAULT
            isChannelEnableVibrate = false
            channelLockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
    }

    /** Represents data needed for BigPictureStyle Notification.  */
    class BigPictureStyleSocialAppData private constructor() : MockNotificationData() {
        // Unique data for this Notification.Style:
        val bigImage: Int
        val bigContentTitle: String
        val summaryText: String
        val possiblePostResponses: Array<CharSequence>
        val participants: ArrayList<String>

        override fun toString(): String {
            return "$contentTitle - $contentText"
        }

        companion object {
            private var sInstance: BigPictureStyleSocialAppData? = null
            val instance: BigPictureStyleSocialAppData?
                get() {
                    if (sInstance == null) {
                        sInstance =
                            sync
                    }
                    return sInstance
                }

            @get:Synchronized
            private val sync: BigPictureStyleSocialAppData?
                private get() {
                    if (sInstance == null) {
                        sInstance =
                            BigPictureStyleSocialAppData()
                    }
                    return sInstance
                }
        }

        init { // Standard Notification values:
// Title/Content for API <16 (4.0 and below) devices.
            contentTitle = "Bob's Post"
            contentText = "[Picture] Like my shot of Earth?"
            priority = NotificationCompat.PRIORITY_HIGH
            // Style notification values:
            bigImage = R.drawable.earth
            bigContentTitle = "Bob's Post"
            summaryText = "Like my shot of Earth?"
            // This would be possible responses based on the contents of the post.
            possiblePostResponses = arrayOf("Yes", "No", "Maybe?")
            participants = ArrayList()
            participants.add("Bob Smith")
            // Notification channel values (for devices targeting 26 and above):
            channelId = "channel_social_1"
            // The user-visible name of the channel.
            channelName = "Sample Social"
            // The user-visible description of the channel.
            channelDescription = "Sample Social Notifications"
            channelImportance = NotificationManager.IMPORTANCE_HIGH
            isChannelEnableVibrate = true
            channelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
    }

    /** Represents data needed for InboxStyle Notification.  */
    class InboxStyleEmailAppData private constructor() : MockNotificationData() {
        // Unique data for this Notification.Style:
        val numberOfNewEmails: Int
        val bigContentTitle: String
        val summaryText: String
        val individualEmailSummary: ArrayList<String>
        val participants: ArrayList<String>

        override fun toString(): String {
            return "$contentTitle $contentText"
        }

        companion object {
            private var sInstance: InboxStyleEmailAppData? = null
            val instance: InboxStyleEmailAppData?
                get() {
                    if (sInstance == null) {
                        sInstance =
                            sync
                    }
                    return sInstance
                }

            @get:Synchronized
            private val sync: InboxStyleEmailAppData?
                private get() {
                    if (sInstance == null) {
                        sInstance = InboxStyleEmailAppData()
                    }
                    return sInstance
                }
        }

        init { // Standard Notification values:
// Title/Content for API <16 (4.0 and below) devices.
            contentTitle = "5 new emails"
            contentText = "from Jane, Jay, Alex +2 more"
            numberOfNewEmails = 5
            priority = NotificationCompat.PRIORITY_DEFAULT
            // Style notification values:
            bigContentTitle = "5 new emails from Jane, Jay, Alex +2"
            summaryText = "New emails"
            // Add each summary line of the new emails, you can add up to 5.
            individualEmailSummary = ArrayList()
            individualEmailSummary.add("Jane Faab  -   Launch Party is here...")
            individualEmailSummary.add("Jay Walker -   There's a turtle on the server!")
            individualEmailSummary.add("Alex Chang -   Check this out...")
            individualEmailSummary.add("Jane Johns -   Check in code?")
            individualEmailSummary.add("John Smith -   Movies later....")
            // If the phone is in "Do not disturb mode, the user will still be notified if
// the user(s) is starred as a favorite.
            participants = ArrayList()
            participants.add("Jane Faab")
            participants.add("Jay Walker")
            participants.add("Alex Chang")
            participants.add("Jane Johns")
            participants.add("John Smith")
            // Notification channel values (for devices targeting 26 and above):
            channelId = "channel_email_1"
            // The user-visible name of the channel.
            channelName = "Sample Email"
            // The user-visible description of the channel.
            channelDescription = "Sample Email Notifications"
            channelImportance = NotificationManager.IMPORTANCE_DEFAULT
            isChannelEnableVibrate = true
            channelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
    }

    /** Represents data needed for MessagingStyle Notification.  */
    class MessagingStyleCommsAppData private constructor(context: Context) :
        MockNotificationData() {
        // Unique data for this Notification.Style:
        val messages: ArrayList<NotificationCompat.MessagingStyle.Message>
        // String of all mMessages.
        val fullConversation: String
        // Name preferred when replying to chat.
        val me: Person
        val participants: ArrayList<Person>
        val replyChoicesBasedOnLastMessage: Array<CharSequence>

        val numberOfNewMessages: Int
            get() = messages.size

        override fun toString(): String {
            return fullConversation
        }

        val isGroupConversation: Boolean
            get() = participants.size > 1

        companion object {
            private var sInstance: MessagingStyleCommsAppData? = null
            fun getInstance(context: Context): MessagingStyleCommsAppData? {
                if (sInstance == null) {
                    sInstance =
                        getSync(context)
                }
                return sInstance
            }

            @Synchronized
            private fun getSync(context: Context): MessagingStyleCommsAppData? {
                if (sInstance == null) {
                    sInstance =
                        MessagingStyleCommsAppData(context)
                }
                return sInstance
            }
        }

        init { // Standard notification values:
// Content for API <24 (M and below) devices.
// Note: I am actually hardcoding these Strings based on info below. You would be
// pulling these values from the same source in your database. I leave this up here, so
// you can see the standard parts of a Notification first.
            contentTitle = "3 Messages w/ Famous, Wendy"
            contentText = "HEY, I see my house! :)"
            priority = NotificationCompat.PRIORITY_HIGH
            // Create the users for the conversation.
// Name preferred when replying to chat.
            me = Person.Builder()
                .setName("Me MacDonald")
                .setKey("1234567890")
                .setUri("tel:1234567890")
                .setIcon(
                    IconCompat.createWithResource(context, R.drawable.me_macdonald)
                )
                .build()
            val participant1 = Person.Builder()
                .setName("Famous Frank")
                .setKey("9876543210")
                .setUri("tel:9876543210")
                .setIcon(
                    IconCompat.createWithResource(context, R.drawable.famous_fryer)
                )
                .build()
            val participant2 = Person.Builder()
                .setName("Wendy Weather")
                .setKey("2233221122")
                .setUri("tel:2233221122")
                .setIcon(IconCompat.createWithResource(context, R.drawable.wendy_wonda))
                .build()
            // If the phone is in "Do not disturb mode, the user will still be notified if
// the user(s) is starred as a favorite.
// Note: You don't need to add yourself, aka 'me', as a participant.
            participants = ArrayList()
            participants.add(participant1)
            participants.add(participant2)
            messages =
                ArrayList()
            // For each message, you need the timestamp. In this case, we are using arbitrary longs
// representing time in milliseconds.
            messages.add( // When you are setting an image for a message, text does not display.
                NotificationCompat.MessagingStyle.Message(
                    "",
                    1528490641998L,
                    participant1
                )
                    .setData("image/png", resourceToUri(context, R.drawable.earth))
            )
            messages.add(
                NotificationCompat.MessagingStyle.Message(
                    "Visiting the moon again? :P", 1528490643998L, me
                )
            )
            messages.add(
                NotificationCompat.MessagingStyle.Message(
                    "HEY, I see my house!",
                    1528490645998L,
                    participant2
                )
            )
            // String version of the mMessages above.
            fullConversation = ("Famous: [Picture of Moon]\n\n"
                    + "Me: Visiting the moon again? :P\n\n"
                    + "Wendy: HEY, I see my house! :)\n\n")
            // Responses based on the last messages of the conversation. You would use
// Machine Learning to get these (https://developers.google.com/ml-kit/).
            replyChoicesBasedOnLastMessage = arrayOf(
                "Me too!",
                "How's the weather?",
                "You have good eyesight."
            )
            // Notification channel values (for devices targeting 26 and above):
            channelId = "channel_messaging_1"
            // The user-visible name of the channel.
            channelName = "Sample Messaging"
            // The user-visible description of the channel.
            channelDescription = "Sample Messaging Notifications"
            channelImportance = NotificationManager.IMPORTANCE_MAX
            isChannelEnableVibrate = true
            channelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
    }

    /** Represents standard data needed for a Notification.  */
    abstract class MockNotificationData {
        // Notification Standard notification get methods:
        // Standard notification values:
        var contentTitle: String? = null
            protected set
        var contentText: String? = null
            protected set
        var priority = 0
            protected set
        // Channel values (O and above) get methods:
        // Notification channel values (O and above):
        var channelId: String? = null
            protected set
        var channelName: CharSequence? = null
            protected set
        var channelDescription: String? = null
            protected set
        var channelImportance = 0
            protected set
        var isChannelEnableVibrate = false
            protected set
        var channelLockscreenVisibility = 0
            protected set

    }
}