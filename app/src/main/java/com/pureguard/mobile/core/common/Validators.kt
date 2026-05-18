package com.pureguard.mobile.core.common

import android.annotation.TargetApi
import android.os.Build
import android.util.Patterns

@Suppress("unused")
object Validator {

    /* -------------------------------------------------------------------------- */
    /*                                    EMAIL                                    */
    /* -------------------------------------------------------------------------- */

    @TargetApi(Build.VERSION_CODES.FROYO)
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    /* -------------------------------------------------------------------------- */
    /*                                  PASSWORD                                   */
    /* -------------------------------------------------------------------------- */

    fun isValidPassword(password: String): Boolean {

        if (password.length < 8) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any {
            !it.isLetterOrDigit()
        }

        return hasUpperCase &&
                hasLowerCase &&
                hasDigit &&
                hasSpecial
    }

    fun passwordValidationMessage(password: String): String {
        return when {
            password.length < 8 ->
                "Password must be at least 8 characters"

            password.none { it.isUpperCase() } ->
                "Password must contain uppercase letter"

            password.none { it.isLowerCase() } ->
                "Password must contain lowercase letter"

            password.none { it.isDigit() } ->
                "Password must contain number"

            password.none { !it.isLetterOrDigit() } ->
                "Password must contain special character"

            else -> "Valid"
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                                   USERNAME                                  */
    /* -------------------------------------------------------------------------- */

    fun isValidUsername(username: String): Boolean {

        if (username.length !in 3..20) return false

        return username.matches(
            Regex("^[a-zA-Z0-9_]+$")
        )
    }

    /* -------------------------------------------------------------------------- */
    /*                                      PIN                                    */
    /* -------------------------------------------------------------------------- */

    fun isValidPin(pin: String): Boolean {
        return pin.matches(Regex("^\\d{4,6}$"))
    }

    /* -------------------------------------------------------------------------- */
    /*                                   DEVICE NAME                               */
    /* -------------------------------------------------------------------------- */

    fun isValidDeviceName(name: String): Boolean {
        return name.trim().length in 2..30
    }

    /* -------------------------------------------------------------------------- */
    /*                                     OTP                                     */
    /* -------------------------------------------------------------------------- */

    fun isValidOtp(otp: String): Boolean {
        return otp.matches(Regex("^\\d{6}$"))
    }

    /* -------------------------------------------------------------------------- */
    /*                                  EMPTY CHECK                                */
    /* -------------------------------------------------------------------------- */

    fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }

    fun arePasswordsMatching(
        password: String,
        confirmPassword: String
    ): Boolean {
        return password == confirmPassword
    }

    /* -------------------------------------------------------------------------- */
    /*                                  PACKAGE NAME                               */
    /* -------------------------------------------------------------------------- */

    fun isValidPackageName(packageName: String): Boolean {

        return packageName.matches(
            Regex("^[a-zA-Z0-9_.]+$")
        )
    }
}