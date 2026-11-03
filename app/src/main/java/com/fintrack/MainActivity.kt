package com.fintrack

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.security.canUseAppLock
import com.fintrack.security.showUnlockPrompt
import com.fintrack.ui.dashboard.DashboardScreen
import com.fintrack.ui.dashboard.DashboardViewModel
import com.fintrack.ui.lock.LockScreen
import com.fintrack.ui.theme.FinTrackTheme

// FragmentActivity is required by BiometricPrompt.
class MainActivity : FragmentActivity() {

    private var locked by mutableStateOf(false)
    private var lockError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val lockStore = (application as FinTrackApp).lockStore
        // Never lock the user out: if the device lost its screen lock, turn the app lock off.
        if (lockStore.enabled.value && !canUseAppLock(this)) lockStore.setEnabled(false)
        // Lock on cold start only. A rotation keeps the current unlocked state.
        if (savedInstanceState == null && lockStore.enabled.value) locked = true
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && lockStore.enabled.value) locked = true
        })

        setContent {
            FinTrackTheme {
                if (locked) {
                    LockScreen(error = lockError, onUnlock = ::requestUnlock)
                } else {
                    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                    DashboardScreen(viewModel)
                }
            }
        }
    }

    private fun requestUnlock() {
        showUnlockPrompt(
            activity = this,
            onSuccess = {
                locked = false
                lockError = null
            },
            onError = { lockError = it.toString() },
        )
    }
}
