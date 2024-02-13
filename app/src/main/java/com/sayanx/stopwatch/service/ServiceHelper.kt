package com.sayanx.stopwatch.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import com.sayanx.stopwatch.MainActivity
import com.sayanx.stopwatch.util.Constant

@ExperimentalAnimationApi
object ServiceHelper {

    private val flag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE
        else
            0

    fun clickPendingIntent(context: Context): PendingIntent {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(Constant.STOPWATCH_STATE, StopwatchState.Started.name)
        }
        return PendingIntent.getActivity(
            context, Constant.CLICK_REQUEST_CODE, clickIntent, flag
        )
    }

    fun stopPendingIntent(context: Context): PendingIntent {
        val stopIntent = Intent(context, StopwatchService::class.java).apply {
            putExtra(Constant.STOPWATCH_STATE, StopwatchState.Stopped.name)
        }
        return PendingIntent.getService(
            context, Constant.STOP_REQUEST_CODE, stopIntent, flag
        )
    }

    fun resumePendingIntent(context: Context): PendingIntent {
        val resumeIntent = Intent(context, StopwatchService::class.java).apply {
            putExtra(Constant.STOPWATCH_STATE, StopwatchState.Started.name)
        }
        return PendingIntent.getService(
            context, Constant.RESUME_REQUEST_CODE, resumeIntent, flag
        )
    }

    fun cancelPendingIntent(context: Context): PendingIntent {
        val cancelIntent = Intent(context, StopwatchService::class.java).apply {
            putExtra(Constant.STOPWATCH_STATE, StopwatchState.Canceled.name)
        }
        return PendingIntent.getService(
            context, Constant.CANCEL_REQUEST_CODE, cancelIntent, flag
        )
    }

    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, StopwatchService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}