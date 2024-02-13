package com.sayanx.stopwatch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sayanx.stopwatch.R
import com.sayanx.stopwatch.service.ServiceHelper
import com.sayanx.stopwatch.service.StopwatchService
import com.sayanx.stopwatch.service.StopwatchState
import com.sayanx.stopwatch.util.Constant

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StopWatchScreen(stopwatchService: StopwatchService) {

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val fontSize = rememberSaveable(key = screenWidthDp.toString()) {
        mutableFloatStateOf(screenWidthDp * 0.18f)
    }
    val buttonMaxSize = rememberSaveable(key = screenWidthDp.toString()) {
        mutableFloatStateOf(screenWidthDp.div(3.2f))
    }

    val context = LocalContext.current
    val hours by stopwatchService.hours
    val minutes by stopwatchService.minutes
    val seconds by stopwatchService.seconds
    val currentState by stopwatchService.currentState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.weight(weight = 8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShowTimeText(hours, "Hour", fontSize.floatValue)
            ShowDot(fontSize = fontSize.floatValue)
            ShowTimeText(minutes, "Minute", fontSize.floatValue)
            ShowDot(fontSize = fontSize.floatValue)
            ShowTimeText(seconds, "Second", fontSize.floatValue)
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            if (currentState == StopwatchState.Stopped || currentState == StopwatchState.Started) {
                Button(
                    modifier = Modifier.width(buttonMaxSize.floatValue.dp),
                    onClick = {
                        ServiceHelper.triggerForegroundService(
                            context = context, action = Constant.ACTION_SERVICE_CANCEL
                        )
                    },
                    enabled = currentState == StopwatchState.Stopped,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                    )
                    Text(
                        text = "Cancel"
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
            Button(
                modifier = Modifier.width(buttonMaxSize.floatValue.dp),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action =
                        if (currentState == StopwatchState.Started) {
                            Constant.ACTION_SERVICE_STOP
                        } else {
                            Constant.ACTION_SERVICE_START
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,//if (true) Red else Blue,
                    contentColor = MaterialTheme.colorScheme.onPrimary//Color.White
                )
            ) {
                Icon(
                    painter = painterResource(
                        id = when (currentState) {
                            StopwatchState.Started -> {
                                R.drawable.baseline_pause_24
                            }
                            StopwatchState.Stopped -> {
                                R.drawable.baseline_play_arrow_24
                            }
                            else -> {
                                R.drawable.baseline_play_arrow_24
                            }
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = when (currentState) {
                        StopwatchState.Started -> {
                            "Stop"
                        }
                        StopwatchState.Stopped -> {
                            "Resume"
                        }
                        else -> {
                            "Start"
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShowDot(fontSize: Float) {
    Text(
        text = ":",
        style = TextStyle(
            fontSize = (fontSize).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(0.1f)
        )
    )
}

@Composable
private fun ShowTimeText(
    timeCount: String,
    label: String,
    fontSize: Float
) {
    AnimatedContent(
        label = label,
        targetState = timeCount,
        transitionSpec = { customAnimation() },
    ) {
        Text(
            text = it,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeCount == "00") MaterialTheme.colorScheme.onSurface.copy(0.7f) else MaterialTheme.colorScheme.primary
            )
        )
    }
}

private fun customAnimation(duration: Int = 900): ContentTransform {
    return (
            slideInVertically(animationSpec = tween(durationMillis = 10))
                    + fadeIn(animationSpec = tween(durationMillis = 700))
            ).togetherWith(
            slideOutVertically(animationSpec = tween(durationMillis = duration))
                    + fadeOut(animationSpec = tween(durationMillis = duration))
            )
}
