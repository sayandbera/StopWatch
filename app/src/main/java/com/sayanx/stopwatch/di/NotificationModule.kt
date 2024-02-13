package com.sayanx.stopwatch.di

import android.app.NotificationManager
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.NotificationCompat
import com.sayanx.stopwatch.R
import com.sayanx.stopwatch.service.ServiceHelper
import com.sayanx.stopwatch.util.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@ExperimentalAnimationApi
@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("00:00:00")
            .setSmallIcon(R.drawable.baseline_alarm_on_24)
            .setOngoing(true)
            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(context))
            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(context))
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}