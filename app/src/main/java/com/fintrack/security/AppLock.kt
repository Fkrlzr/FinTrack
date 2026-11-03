package com.fintrack.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stores the app lock switch. Local preferences only. */
class AppLockStore(context: Context) {
    private val prefs = context.getSharedPreferences("fintrack_settings", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(prefs.getBoolean(KEY, false))

    val enabled: StateFlow<Boolean> = state.asStateFlow()

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY, value).apply()
        state.value = value
    }

    private companion object {
        const val KEY = "app_lock_enabled"
    }
}

/**
 * Android 11+ accepts strong biometrics or the screen lock PIN/pattern as one option. Older
 * versions only accept biometrics, so the prompt needs its own cancel button.
 */
private fun authenticators(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
        BIOMETRIC_WEAK
    }

fun canUseAppLock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(authenticators()) == BiometricManager.BIOMETRIC_SUCCESS

fun showUnlockPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (CharSequence) -> Unit,
) {
    val auth = authenticators()
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock FinTrack")
        .setAllowedAuthenticators(auth)
        .apply {
            if ((auth and DEVICE_CREDENTIAL) == 0) setNegativeButtonText("Cancel")
        }
        .build()
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError(errString)
        },
    )
    prompt.authenticate(info)
}
