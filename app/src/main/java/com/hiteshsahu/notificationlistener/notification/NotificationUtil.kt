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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.hiteshsahu.notificationlistener.notification.MockDatabase.MockNotificationData

/**
 * Simplifies common [Notification] tasks.
 */
object NotificationUtil {
    @JvmStatic
    fun createNotificationChannel(
        context: Context,
        mockNotificationData: MockNotificationData
    ): String? { // NotificationChannels are required for Notifications on O (API 26) and above.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // The id of the channel.
            val channelId = mockNotificationData.channelId
            // The user-visible name of the channel.
            val channelName = mockNotificationData.channelName
            // The user-visible description of the channel.
            val channelDescription = mockNotificationData.channelDescription
            val channelImportance = mockNotificationData.channelImportance
            val channelEnableVibrate = mockNotificationData.isChannelEnableVibrate
            val channelLockscreenVisibility =
                mockNotificationData.channelLockscreenVisibility
            // Initializes NotificationChannel.
            val notificationChannel =
                NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.description = channelDescription
            notificationChannel.enableVibration(channelEnableVibrate)
            notificationChannel.lockscreenVisibility = channelLockscreenVisibility
            // Adds NotificationChannel to system. Attempting to create an existing notification
// channel with its original values performs no operation, so it's safe to perform the
// below sequence.
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            channelId
        } else { // Returns null for pre-O (26) devices.
            null
        }
    }
}