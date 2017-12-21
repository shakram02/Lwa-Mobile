package shakram02.ahmed.lwa.otp

import android.content.SharedPreferences

/**
 * Holds stored OTP token settings
 */
public class OtpSettingsStore(private val pref: SharedPreferences, private val key: String) {
    companion object {
        private const val KEY_SECRET = "SECRET"
        private const val KEY_TYPE = "TYPE"
        private const val KEY_COUNTER = "KEY_COUNTER"
        private const val VAL_INVALID_STRING = "INVALID"
        private const val VAL_INVALID_INT = -1
    }

    fun save(secret: String, type: OtpType, counter: Int) {
        pref.edit()
                .putString(key + KEY_SECRET, secret)
                .putInt(key + KEY_TYPE, type.value)
                .putInt(key + KEY_COUNTER, counter)
                .apply()
    }

    fun save(secret: String, type: OtpType) {
        pref.edit()
                .putString(key + KEY_SECRET, secret)
                .putInt(key + KEY_TYPE, type.value)
                .apply()
    }

    // TODO HOTP will be added later
    private fun incrementCounter() {
        // TODO remove checking for idiots mistakes
        if (!canLoad()) throw IllegalStateException("Nothing to be loaded")

        val saved: OtpSettings = load()
        save(saved.secret, saved.type, saved.counter + 1)
    }

    /**
     * Checks whether saved settings exist or not
     * @return True if there are saved settings
     */
    fun canLoad(): Boolean {
        return pref.getString(key + KEY_SECRET, VAL_INVALID_STRING) != VAL_INVALID_STRING
    }

    /**
     * Loads the saved config without checking for their existence.
     * Use [OtpSettingsStore.canLoad] before calling this function
     */
    fun load(): OtpSettings {
        val secret = pref.getString(key + KEY_SECRET, VAL_INVALID_STRING)
        val type = OtpType.getEnum(pref.getInt(key + KEY_TYPE, VAL_INVALID_INT))

        return if (type == OtpType.TOTP) {
            OtpSettings(secret, type)
        } else {
            val counter = pref.getInt(key + KEY_COUNTER, VAL_INVALID_INT)
            OtpSettings(secret, type, counter)
        }
    }

    data class OtpSettings(val secret: String, val type: OtpType, val counter: Int) {
        constructor(secret: String, type: OtpType) : this(secret, type, -1)
    }
}