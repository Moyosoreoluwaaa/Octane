package com.octane.domain.usecase.security

import com.octane.core.security.BiometricAvailability
import com.octane.core.security.BiometricManager

/**
 * Checks if biometric authentication is available on device.
 */
class CheckBiometricAvailabilityUseCase(
    private val biometricManager: BiometricManager
) {
    operator fun invoke(): BiometricAvailability {
        return biometricManager.isBiometricAvailable()
    }
}

