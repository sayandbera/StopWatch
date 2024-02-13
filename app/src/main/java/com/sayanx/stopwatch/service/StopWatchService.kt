package com.sayanx.stopwatch.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.sayanx.stopwatch.util.Constant
import com.sayanx.stopwatch.util.formatTime
import com.sayanx.stopwatch.util.pad
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ExperimentalAnimationApi
@AndroidEntryPoint
class StopwatchService : Service() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var notificationBuilder: NotificationCompat.Builder

    private val binder = StopwatchBinder()

    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentState = mutableStateOf(StopwatchState.Idle)
        private set

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(Constant.STOPWATCH_STATE)) {
            StopwatchState.Started.name -> {
                setStopButton()
                startForegroundService()
                startStopwatch { hours, minutes, seconds ->
                    updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                }
            }
            StopwatchState.Stopped.name -> {
                setResumeButton()
                stopStopwatch()
            }
            StopwatchState.Canceled.name -> {
                stopForegroundService()
                stopStopwatch()
                cancelStopwatch()
            }
        }
        intent?.action.let {
            when (it) {
                Constant.ACTION_SERVICE_START -> {
                    startForegroundService()
                    startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
                    setStopButton()
                }
                Constant.ACTION_SERVICE_STOP -> {
                    stopStopwatch()
                    setResumeButton()
                }
                Constant.ACTION_SERVICE_CANCEL -> {
                    stopForegroundService()
                    stopStopwatch()
                    cancelStopwatch()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startStopwatch(onTick: (h: String, m: String, s: String) -> Unit) {
        currentState.value = StopwatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopStopwatch() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentState.value = StopwatchState.Stopped
    }

    private fun cancelStopwatch() {
        duration = Duration.ZERO
        currentState.value = StopwatchState.Idle
        updateTimeUnits()
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@StopwatchService.hours.value = hours.toInt().pad()
            this@StopwatchService.minutes.value = minutes.pad()
            this@StopwatchService.seconds.value = seconds.pad()
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(Constant.NOTIFICATION_ID, notificationBuilder.build())
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun stopForegroundService() {
        GlobalScope.launch(Dispatchers.Default) {
            notificationManager.cancel(Constant.NOTIFICATION_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constant.NOTIFICATION_CHANNEL_ID,
            Constant.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            Constant.NOTIFICATION_ID,
            notificationBuilder.setContentTitle(
                formatTime(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                )
            ).build()
        )
    }

    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(1)
        notificationBuilder.mActions.add(
            1,
            NotificationCompat.Action(
                0,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(Constant.NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(1)
        notificationBuilder.mActions.add(
            1,
            NotificationCompat.Action(
                0,
                "Resume",
                ServiceHelper.resumePendingIntent(this)
            )
        )
        notificationManager.notify(Constant.NOTIFICATION_ID, notificationBuilder.build())
    }

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}

enum class StopwatchState {
    Idle,
    Started,
    Stopped,
    Canceled
}