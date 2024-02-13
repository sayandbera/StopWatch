package com.sayanx.stopwatch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sayanx.stopwatch.service.StopwatchService
import com.sayanx.stopwatch.ui.StopWatchScreen
import com.sayanx.stopwatch.ui.theme.StopWatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(false)
    @OptIn(ExperimentalAnimationApi::class)
    private lateinit var stopwatchService: StopwatchService
    @OptIn(ExperimentalAnimationApi::class)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as StopwatchService.StopwatchBinder
            stopwatchService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onStart() {
        super.onStart()
        Intent(this, StopwatchService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StopWatchTheme {
                if (isBound) {
                    StopWatchScreen(stopwatchService = stopwatchService)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestPermissions(vararg permissions: String) {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.entries.forEach {
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }
        requestPermissionLauncher.launch(permissions.asList().toTypedArray())
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}