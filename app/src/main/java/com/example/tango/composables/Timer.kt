package com.example.tango.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun Timer(
    modifier: Modifier = Modifier,
    running: Boolean = false,
    ticks: Int = 0,
    onTick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    if (running) {
        LaunchedEffect(lifecycleState) {
            while (running && lifecycleState == Lifecycle.State.RESUMED) {
                delay(1.seconds)
                onTick()
            }
        }
    }
    Text(
        "%d:%02d".format(ticks / 60, ticks % 60),
        modifier = modifier
    )
}

@Preview
@Composable
fun TimerPreview() {
    Timer {}
}